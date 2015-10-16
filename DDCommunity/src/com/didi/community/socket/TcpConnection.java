
package com.didi.community.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import static com.didi.community.socket.Log.*;

class TcpConnection {
	static private final int IPTOS_LOWDELAY = 0x10;

	SocketChannel socketChannel;
	int keepAliveMillis = 8000;
	final ByteBuffer readBuffer, writeBuffer;
	boolean bufferPositionFix;
	int timeoutMillis = 12000;
	float idleThreshold = 0.1f;

	final Serialization serialization;
	private SelectionKey selectionKey;
	private volatile long lastWriteTime, lastReadTime;
	private int currentObjectLength;
	private final Object writeLock = new Object();

	public TcpConnection (Serialization serialization, int writeBufferSize, int objectBufferSize) {
		this.serialization = serialization;
		writeBuffer = ByteBuffer.allocate(writeBufferSize);
		readBuffer = ByteBuffer.allocate(objectBufferSize);
		readBuffer.flip();
	}

	//监听到连接后注册通道的读事件
	public SelectionKey accept (Selector selector, SocketChannel socketChannel) throws IOException {
		writeBuffer.clear();
		readBuffer.clear();
		readBuffer.flip();
		currentObjectLength = 0;
		try {
			this.socketChannel = socketChannel;
			socketChannel.configureBlocking(false);
			Socket socket = socketChannel.socket();
			socket.setTcpNoDelay(true);

			selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);

			if (DEBUG) {
				debug("hjh", "Port " + socketChannel.socket().getLocalPort() + "/TCP connected to: "
					+ socketChannel.socket().getRemoteSocketAddress());
			}

			lastReadTime = lastWriteTime = System.currentTimeMillis();

			return selectionKey;
		} catch (IOException ex) {
			close();
			throw ex;
		}
	}

	public void connect (Selector selector, SocketAddress remoteAddress, int timeout) throws IOException {
		close();
		writeBuffer.clear();
		readBuffer.clear();
		readBuffer.flip();
		currentObjectLength = 0;
		try {
			SocketChannel socketChannel = selector.provider().openSocketChannel();
			Socket socket = socketChannel.socket();
			socket.setTcpNoDelay(true);
			// socket.setTrafficClass(IPTOS_LOWDELAY);
			socket.connect(remoteAddress, timeout); // Connect using blocking mode for simplicity.
			socketChannel.configureBlocking(false);
			this.socketChannel = socketChannel;

			selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
			selectionKey.attach(this);

			if (DEBUG) {
				debug("hjh", "Port " + socketChannel.socket().getLocalPort() + "/TCP connected to: "
					+ socketChannel.socket().getRemoteSocketAddress());
			}

			lastReadTime = lastWriteTime = System.currentTimeMillis();
		} catch (IOException ex) {
			close();
			IOException ioEx = new IOException("Unable to connect to: " + remoteAddress);
			ioEx.initCause(ex);
			throw ioEx;
		}
	}

	public String readObject (Connection connection) throws IOException {
		SocketChannel socketChannel = this.socketChannel;
		
		if (socketChannel == null) throw new SocketException("Connection is closed.");

		if (currentObjectLength == 0) {
			// Read the length of the next object from the socket.
			int lengthLength = serialization.getLengthLength();//4字节的包头
			if (readBuffer.remaining() < lengthLength) {
				readBuffer.compact();
		/**如果Buffer中仍有未读的数据，且后续还需要这些数据，但是此时想要先先写些数据，那么使用compact()方法。
		compact()方法将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。
		limit属性依然像clear()方法一样，设置成capacity。现在Buffer准备好写数据了，但是不会覆盖未读的数据。 */
				int bytesRead = socketChannel.read(readBuffer);
				readBuffer.flip();//lip方法将Buffer从写模式切换到读模式。调用flip()方法会将position设回0，并将limit设置成之前position的值
				if (bytesRead == -1) throw new SocketException("Connection is closed.");
				lastReadTime = System.currentTimeMillis();

				if (readBuffer.remaining() < lengthLength) return null;
			}
			currentObjectLength = serialization.readLength(readBuffer);

			if (currentObjectLength <= 0) throw new DiDiNetException("Invalid object length: " + currentObjectLength);
			if (currentObjectLength > readBuffer.capacity())
				throw new DiDiNetException("Unable to read object larger than read buffer: " + currentObjectLength);
		}

		int length = currentObjectLength;
		if (readBuffer.remaining() < length) {//Buffer里面还有原未读数据
			// Fill the tcpInputStream.
			readBuffer.compact();
			int bytesRead = socketChannel.read(readBuffer);
			readBuffer.flip();
			if (bytesRead == -1) throw new SocketException("Connection is closed.");
			lastReadTime = System.currentTimeMillis();

			if (readBuffer.remaining() < length) return null;
		}
		currentObjectLength = 0;

		int startPosition = readBuffer.position();
		int oldLimit = readBuffer.limit();
		readBuffer.limit(startPosition + length);
		String received = serialization.read(connection, readBuffer);
//		System.out.println("hjh ==> receive :"+received);

		readBuffer.limit(oldLimit);
		if (readBuffer.position() - startPosition != length)
			throw new DiDiNetException("Incorrect number of bytes (" + (startPosition + length - readBuffer.position())
				+ " remaining) used to deserialize object: ");

		return received;
	}

	public void writeOperation () throws IOException {
		synchronized (writeLock) {
			if (writeToSocket()) {
				// Write successful, clear OP_WRITE.
				selectionKey.interestOps(SelectionKey.OP_READ);
			}
			lastWriteTime = System.currentTimeMillis();
		}
	}

	private boolean writeToSocket () throws IOException {
		SocketChannel socketChannel = this.socketChannel;
		if (socketChannel == null) throw new SocketException("Connection is closed.");

		ByteBuffer buffer = writeBuffer;
		buffer.flip();
		while (buffer.hasRemaining()) {
			if (bufferPositionFix) {
				buffer.compact();
				buffer.flip();
			}
			if (socketChannel.write(buffer) == 0) break;
		}
		buffer.compact();

		return buffer.position() == 0;
	}

	/** This method is thread safe. */
	public int send (Connection connection, Object object) throws IOException {
		SocketChannel socketChannel = this.socketChannel;
		if (socketChannel == null) throw new SocketException("Connection is closed.");
		
		synchronized (writeLock) {
			// Leave room for length.
			int start = writeBuffer.position();
			int lengthLength = serialization.getLengthLength();
			writeBuffer.position(writeBuffer.position() + lengthLength);
			
			// Write data.
			try {
				serialization.write(connection, writeBuffer,object);
			} catch (DiDiNetException ex) {
				throw new DiDiNetException("Error serializing object of type: " + object.toString(), ex);
			}
			int end = writeBuffer.position();

			// Write data length.
			writeBuffer.position(start);
			serialization.writeLength(writeBuffer, end - lengthLength - start);
			writeBuffer.position(end);

			// Write to socket if no data was queued.
			if (start == 0 && !writeToSocket()) {
				// A partial write, set OP_WRITE to be notified when more writing can occur.
				selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			} else {
				// Full write, wake up selector so idle event will be fired.
				selectionKey.selector().wakeup();
			}

			if (DEBUG || TRACE) {
				float percentage = writeBuffer.position() / (float)writeBuffer.capacity();
				if (DEBUG && percentage > 0.75f)
					debug("hjh", connection + " TCP write buffer is approaching capacity: " + percentage + "%");
				else if (TRACE && percentage > 0.25f)
					trace("hjh", connection + " TCP write buffer utilization: " + percentage + "%");
			}

			lastWriteTime = System.currentTimeMillis();
			return end - start;
		}
	}

	public void close () {
		try {
			if (socketChannel != null) {
				socketChannel.close();
				socketChannel = null;
				if (selectionKey != null) selectionKey.selector().wakeup();
			}
		} catch (IOException ex) {
			if (DEBUG) debug("hjh", "Unable to close TCP connection.", ex);
		}
	}

	public boolean needsKeepAlive (long time) {//超过8秒未写存活事件，则需要存活
//		System.out.println("needsKeepAlive : "+(time - lastWriteTime));
		return socketChannel != null && keepAliveMillis > 0 && time - lastWriteTime > keepAliveMillis;
	}

	public boolean isTimedOut (long time) {//超过12秒无读事件任务超时，服务器主动关闭其连接
		return socketChannel != null && timeoutMillis > 0 && time - lastReadTime > timeoutMillis;
	}
}
