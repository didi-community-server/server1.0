
package com.didi.community.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import com.didi.community.socket.FrameworkMessage.Ping;


import static com.didi.community.socket.Log.*;


public class Connection {
	int id = -1;//连接id数量标志
	private String name;
	EndPoint endPoint;
	TcpConnection tcp;
	UdpConnection udp;
	InetSocketAddress udpRemoteAddress;
	private Listener[] listeners = {};
	private Object listenerLock = new Object();
	private int lastPingID;
	private long lastPingSendTime;
	private int returnTripTime;
	volatile boolean isConnected;
	private SocketChannel clientChannel;//连接对应的客户端通道
	private volatile int clientId;//客户端对应的id

	protected Connection () {
	}

	void initialize (Serialization serialization, int writeBufferSize, int objectBufferSize) {
		tcp = new TcpConnection(serialization, writeBufferSize, objectBufferSize);
	}

	/** Returns the server assigned ID. Will return -1 if this connection has never been connected or the last assigned ID if this
	 * connection has been disconnected. */
	public int getID () {
		return id;
	}

	/** Returns true if this connection is connected to the remote end. Note that a connection can become disconnected at any time. */
	public boolean isConnected () {
		return isConnected;
	}

	public int sendTCP (Object object) {
		if (object == null) throw new IllegalArgumentException("object cannot be null.");
		try {
			int length = tcp.send(this, object);
			if (length == 0) {
				if (TRACE) trace("hjh", this + " TCP had nothing to send.");
			} else if (DEBUG) {
				String objectString = object == null ? "null" : object.getClass().getSimpleName();
			}
			return length;
		} catch (IOException ex) {
			if (DEBUG) debug("hjh", "Unable to send TCP with connection: " + this, ex);
			close();
			return 0;
		} catch (DiDiNetException ex) {
			if (ERROR) error("hjh", "Unable to send TCP with connection: " + this, ex);
			close();
			return 0;
		}
	}

	public int sendUDP (Object object) {
		if (object == null) throw new IllegalArgumentException("object cannot be null.");
		SocketAddress address = udpRemoteAddress;
		if (address == null && udp != null) address = udp.connectedAddress;
		if (address == null && isConnected) throw new IllegalStateException("Connection is not connected via UDP.");

		try {
			if (address == null) throw new SocketException("Connection is closed.");

			int length = udp.send(this, object, address);
			if (length == 0) {
				if (TRACE) trace("hjh", this + " UDP had nothing to send.");
			} else if (DEBUG) {
				if (length != -1) {
					String objectString = object == null ? "null" : object.getClass().getSimpleName();
					if (!(object instanceof FrameworkMessage)) {
						debug("hjh", this + " sent UDP: " + objectString + " (" + length + ")");
					} else if (TRACE) {
						trace("hjh", this + " sent UDP: " + objectString + " (" + length + ")");
					}
				} else
					debug("hjh", this + " was unable to send, UDP socket buffer full.");
			}
			return length;
		} catch (IOException ex) {
			if (DEBUG) debug("hjh", "Unable to send UDP with connection: " + this, ex);
			close();
			return 0;
		} catch (DiDiNetException ex) {
			if (ERROR) error("hjh", "Unable to send UDP with connection: " + this, ex);
			close();
			return 0;
		}
	}

	public void close () {
		boolean wasConnected = isConnected;
		isConnected = false;
		tcp.close();
		if (udp != null && udp.connectedAddress != null) udp.close();
		if (wasConnected) {
			notifyDisconnected();
			if (INFO) info("hjh", this + " disconnected.");
		}
		setConnected(false);
	}

	public void updateReturnTripTime () {
		Ping ping = new Ping();
		ping.id = lastPingID++;
		lastPingSendTime = System.currentTimeMillis();
		sendTCP(ping);
	}
	
	public int getReturnTripTime () {
		return returnTripTime;
	}

	/** An empty object will be sent if the TCP connection has not sent an object within the specified milliseconds. Periodically
	 * sending a keep alive ensures that an abnormal close is detected in a reasonable amount of time (see {@link #setTimeout(int)}
	 * ). Also, some network hardware will close a TCP connection that ceases to transmit for a period of time (typically 1+
	 * minutes). Set to zero to disable. Defaults to 8000. */
	public void setKeepAliveTCP (int keepAliveMillis) {
		tcp.keepAliveMillis = keepAliveMillis;
	}

	public void setTimeout (int timeoutMillis) {
		tcp.timeoutMillis = timeoutMillis;
	}

	/** If the listener already exists, it is not added again. */
	public void addListener (Listener listener) {
		if (listener == null) throw new IllegalArgumentException("listener cannot be null.");
		synchronized (listenerLock) {
			Listener[] listeners = this.listeners;
			int n = listeners.length;
			for (int i = 0; i < n; i++)
				if (listener == listeners[i]) return;
			Listener[] newListeners = new Listener[n + 1];
			newListeners[0] = listener;
			System.arraycopy(listeners, 0, newListeners, 1, n);
			this.listeners = newListeners;
		}
		if (TRACE) trace("hjh", "Connection listener added: " + listener.getClass().getName());
	}

	public void removeListener (Listener listener) {
		if (listener == null) throw new IllegalArgumentException("listener cannot be null.");
		synchronized (listenerLock) {
			Listener[] listeners = this.listeners;
			int n = listeners.length;
			if (n == 0) return;
			Listener[] newListeners = new Listener[n - 1];
			for (int i = 0, ii = 0; i < n; i++) {
				Listener copyListener = listeners[i];
				if (listener == copyListener) continue;
				if (ii == n - 1) return;
				newListeners[ii++] = copyListener;
			}
			this.listeners = newListeners;
		}
		if (TRACE) trace("hjh", "Connection listener removed: " + listener.getClass().getName());
	}

	void notifyConnected () {
		if (INFO) {
			SocketChannel socketChannel = tcp.socketChannel;
			if (socketChannel != null) {
				Socket socket = tcp.socketChannel.socket();
				if (socket != null) {
					InetSocketAddress remoteSocketAddress = (InetSocketAddress)socket.getRemoteSocketAddress();
					if (remoteSocketAddress != null) info("hjh", this + " connected: " + remoteSocketAddress.getAddress());
				}
			}
		}
		Listener[] listeners = this.listeners;
		for (int i = 0, n = listeners.length; i < n; i++)
			listeners[i].connected(this);
	}

	void notifyDisconnected () {
		Listener[] listeners = this.listeners;
		for (int i = 0, n = listeners.length; i < n; i++)
			listeners[i].disconnected(this);
	}

	void notifyIdle () {
		Listener[] listeners = this.listeners;
		for (int i = 0, n = listeners.length; i < n; i++) {
			listeners[i].idle(this);
			if (!isIdle()) break;
		}
	}

	void notifyReceived (Object object) {
		if (object instanceof Ping) {
			Ping ping = (Ping)object;
			if (ping.isReply) {
				if (ping.id == lastPingID - 1) {
					returnTripTime = (int)(System.currentTimeMillis() - lastPingSendTime);
					if (TRACE) trace("hjh", this + " return trip time: " + returnTripTime);
				}
			} else {
				ping.isReply = true;
				sendTCP(ping);
			}
		}
		Listener[] listeners = this.listeners;
		for (int i = 0, n = listeners.length; i < n; i++)
			listeners[i].received(this, object);
	}

	/** Returns the local {@link Client} or {@link Server} to which this connection belongs. */
	public EndPoint getEndPoint () {
		return endPoint;
	}

	/** Returns the IP address and port of the remote end of the TCP connection, or null if this connection is not connected. */
	public InetSocketAddress getRemoteAddressTCP () {
		SocketChannel socketChannel = tcp.socketChannel;
		if (socketChannel != null) {
			Socket socket = tcp.socketChannel.socket();
			if (socket != null) {
				return (InetSocketAddress)socket.getRemoteSocketAddress();
			}
		}
		return null;
	}

	/** Returns the IP address and port of the remote end of the UDP connection, or null if this connection is not connected. */
	public InetSocketAddress getRemoteAddressUDP () {
		InetSocketAddress connectedAddress = udp.connectedAddress;
		if (connectedAddress != null) return connectedAddress;
		return udpRemoteAddress;
	}

	/** Workaround for broken NIO networking on Android 1.6. If true, the underlying NIO buffer is always copied to the beginning of
	 * the buffer before being given to the SocketChannel for sending. The Harmony SocketChannel implementation in Android 1.6
	 * ignores the buffer position, always copying from the beginning of the buffer. This is fixed in Android 2.0+. */
	public void setBufferPositionFix (boolean bufferPositionFix) {
		tcp.bufferPositionFix = bufferPositionFix;
	}

	/** Sets the friendly name of this connection. This is returned by {@link #toString()} and is useful for providing application
	 * specific identifying information in the logging. May be null for the default name of "Connection X", where X is the
	 * connection ID. */
	public void setName (String name) {
		this.name = name;
	}

	/** Returns the number of bytes that are waiting to be written to the TCP socket, if any. */
	public int getTcpWriteBufferSize () {
		return tcp.writeBuffer.position();
	}

	/** @see #setIdleThreshold(float) */
	public boolean isIdle () {
		return tcp.writeBuffer.position() / (float)tcp.writeBuffer.capacity() < tcp.idleThreshold;
	}

	/** If the percent of the TCP write buffer that is filled is less than the specified threshold,
	 * {@link Listener#idle(Connection)} will be called for each network thread update. Default is 0.1. */
	public void setIdleThreshold (float idleThreshold) {
		tcp.idleThreshold = idleThreshold;
	}

	public String toString () {
		if (name != null) return name;
		return "Connection " + id;
	}

	void setConnected (boolean isConnected) {
		this.isConnected = isConnected;
		if (isConnected && name == null) name = "Connection " + id;
	}

	public SocketChannel getClientChannel() {
		return clientChannel;
	}

	public void setClientChannel(SocketChannel clientChannel) {
		this.clientChannel = clientChannel;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	
	
}
