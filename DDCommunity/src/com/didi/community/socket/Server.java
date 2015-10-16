
package com.didi.community.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hjh.request.bean.SocketFromType;
import org.hjh.request.bean.SocketMessage;
import org.hjh.request.bean.SocketPresence;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.didi.community.bean.Shop;
import com.didi.community.bean.ShopUndoMsg;
import com.didi.community.bean.User;
import com.didi.community.service.UserService;
import com.didi.community.socket.FrameworkMessage.DiscoverHost;
import com.didi.community.socket.FrameworkMessage.RegisterTCP;
import com.didi.community.socket.FrameworkMessage.RegisterUDP;
import com.google.gson.Gson;

import static com.didi.community.socket.Log.*;



public class Server implements EndPoint {
	private final Serialization serialization;
	private final int writeBufferSize, objectBufferSize;
	private final Selector selector;
	private int emptySelects;
	private ServerSocketChannel serverChannel;
	private UdpConnection udp;
	private volatile Connection[] connections = {};
	private IntMap<Connection> pendingConnections = new IntMap();
	Listener[] listeners = {};
	private Object listenerLock = new Object();
	private int nextConnectionID = 1;
	private volatile boolean shutdown;
	private Object updateLock = new Object();
	private Thread updateThread;
	private ServerDiscoveryHandler discoveryHandler;
	private Gson gson;
	ApplicationContext factory = new ClassPathXmlApplicationContext("applicationContext.xml");
	UserService service = (UserService) factory.getBean("userService");
	
	public Server(Gson gson){
		this(16384, 2048);
		this.gson = gson;
	}

	private Listener dispatchListener = new Listener() {
		public void connected (Connection connection) {
			Listener[] listeners = Server.this.listeners;
			for (int i = 0, n = listeners.length; i < n; i++)
				listeners[i].connected(connection);
		}

		public void disconnected (Connection connection) {
			removeConnection(connection);
			Listener[] listeners = Server.this.listeners;
			for (int i = 0, n = listeners.length; i < n; i++)
				listeners[i].disconnected(connection);
		}

		public void received (Connection connection, Object object) {
			Listener[] listeners = Server.this.listeners;
			for (int i = 0, n = listeners.length; i < n; i++)
				listeners[i].received(connection, object);
		}

		public void idle (Connection connection) {
			Listener[] listeners = Server.this.listeners;
			for (int i = 0, n = listeners.length; i < n; i++)
				listeners[i].idle(connection);
		}
	};

	/** Creates a Server with a write buffer size of 16384 and an object buffer size of 2048. */
	public Server () {
		this(16384, 2048);
	}

	/** @param writeBufferSize One buffer of this size is allocated for each connected client. Objects are serialized to the write
	 *           buffer where the bytes are queued until they can be written to the TCP socket.
	 *           <p>
	 *           Normally the socket is writable and the bytes are written immediately. If the socket cannot be written to and
	 *           enough serialized objects are queued to overflow the buffer, then the connection will be closed.
	 *           <p>
	 *           The write buffer should be sized at least as large as the largest object that will be sent, plus some head room to
	 *           allow for some serialized objects to be queued in case the buffer is temporarily not writable. The amount of head
	 *           room needed is dependent upon the size of objects being sent and how often they are sent.
	 * @param objectBufferSize One (using only TCP) or three (using both TCP and UDP) buffers of this size are allocated. These
	 *           buffers are used to hold the bytes for a single object graph until it can be sent over the network or
	 *           deserialized.
	 *           <p>
	 *           The object buffers should be sized at least as large as the largest object that will be sent or received. */
	public Server (int writeBufferSize, int objectBufferSize) {
		this(writeBufferSize, objectBufferSize, new DiDiSerialization());
	}

	public Server (int writeBufferSize, int objectBufferSize, Serialization serialization) {
		this.writeBufferSize = writeBufferSize;
		this.objectBufferSize = objectBufferSize;

		this.serialization = serialization;

		this.discoveryHandler = ServerDiscoveryHandler.DEFAULT;

		try {
			selector = Selector.open();
		} catch (IOException ex) {
			throw new RuntimeException("Error opening selector.", ex);
		}
	}

	public void setDiscoveryHandler (ServerDiscoveryHandler newDiscoveryHandler) {
		discoveryHandler = newDiscoveryHandler;
	}

	public Serialization getSerialization () {
		return serialization;
	}

	/** Opens a TCP only server.
	 * @throws IOException if the server could not be opened. */
	public void bind (int tcpPort) throws IOException {
		bind(new InetSocketAddress(tcpPort), null);
	}

	/** Opens a TCP and UDP server.
	 * @throws IOException if the server could not be opened. */
	public void bind (int tcpPort, int udpPort) throws IOException {
		bind(new InetSocketAddress(tcpPort), new InetSocketAddress(udpPort));
	}

	/** @param udpPort May be null. */
	public void bind (InetSocketAddress tcpPort, InetSocketAddress udpPort) throws IOException {
		close();
		synchronized (updateLock) {
			selector.wakeup();
			try {
				serverChannel = selector.provider().openServerSocketChannel();
				serverChannel.socket().bind(tcpPort);
				serverChannel.configureBlocking(false);
				serverChannel.register(selector, SelectionKey.OP_ACCEPT);
				if (DEBUG) debug("hjh", "Accepting connections on port: " + tcpPort + "/TCP");

				if (udpPort != null) {
					udp = new UdpConnection(serialization, objectBufferSize);
					udp.bind(selector, udpPort);
					if (DEBUG) debug("hjh", "Accepting connections on port: " + udpPort + "/UDP");
				}
			} catch (IOException ex) {
				close();
				throw ex;
			}
		}
		if (INFO) info("hjh", "Server opened.");
	}

	/** Accepts any new connections and reads or writes any pending data for the current connections.
	 * @param timeout Wait for up to the specified milliseconds for a connection to be ready to process. May be zero to return
	 *           immediately if there are no connections to process. */
	public void update (int timeout) throws IOException {
		updateThread = Thread.currentThread();
		synchronized (updateLock) { // Blocks to avoid a select while the selector is used to bind the server connection.
		}
		long startTime = System.currentTimeMillis();
		int select = 0;
		if (timeout > 0) {
			select = selector.select(timeout);
		} else {
			select = selector.selectNow();
		}
		if (select == 0) {
			emptySelects++;
			if (emptySelects == 100) {
				emptySelects = 0;
				// NIO freaks and returns immediately with 0 sometimes, so try to keep from hogging the CPU.
				long elapsedTime = System.currentTimeMillis() - startTime;
				try {
					if (elapsedTime < 25) Thread.sleep(25 - elapsedTime);
				} catch (InterruptedException ex) {
				}
			}
		} else {
			emptySelects = 0;
			Set<SelectionKey> keys = selector.selectedKeys();
			synchronized (keys) {
				UdpConnection udp = this.udp;
				outer:
				for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext();) {
					keepAlive();//保持每个连接的存活
					SelectionKey selectionKey = iter.next();
					iter.remove();
					Connection fromConnection = (Connection)selectionKey.attachment();
					try {
						int ops = selectionKey.readyOps();

						if (fromConnection != null) { // Must be a TCP read or write operation.
							if (udp != null && fromConnection.udpRemoteAddress == null) {
								fromConnection.close();
								continue;
							}
							if ((ops & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
								try {
									while (true) {
										Object object = fromConnection.tcp.readObject(fromConnection);
										if (object == null) break;
//										if (DEBUG) {
//											if (!(object instanceof FrameworkMessage)) {
//												debug("hjh", fromConnection + " send TCP: " + object.toString());
//											} else if (TRACE) {
//												trace("hjh", fromConnection + " send TCP: " + object.toString());
//											}
//										}
										fromConnection.notifyReceived(object);
									}
								} catch (IOException ex) {
									if (TRACE) {
										trace("hjh", "Unable to read TCP from: " + fromConnection, ex);
									} else if (DEBUG) {
										debug("hjh", fromConnection + " update: " + ex.getMessage());
									}
									fromConnection.close();
								} catch (DiDiNetException ex) {
									if (ERROR) error("hjh", "Error reading TCP from connection: " + fromConnection, ex);
									fromConnection.close();
								}
							}
							if ((ops & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
								try {
									fromConnection.tcp.writeOperation();
								} catch (IOException ex) {
									if (TRACE) {
										trace("hjh", "Unable to write TCP to connection: " + fromConnection, ex);
									} else if (DEBUG) {
										debug("hjh", fromConnection + " update: " + ex.getMessage());
									}
									fromConnection.close();
								}
							}
							continue;
						}

						if ((ops & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
							ServerSocketChannel serverChannel = this.serverChannel;
							if (serverChannel == null) continue;
							try {
								SocketChannel socketChannel = serverChannel.accept();
								if (socketChannel != null) acceptOperation(socketChannel);
							} catch (IOException ex) {
								if (DEBUG) debug("hjh", "Unable to accept new connection.", ex);
							}
							continue;
						}

						// Must be a UDP read operation.
						if (udp == null) {
							selectionKey.channel().close();
							continue;
						}
						InetSocketAddress fromAddress;
						try {
							fromAddress = udp.readFromAddress();
						} catch (IOException ex) {
							if (WARN) warn("hjh", "Error reading UDP data.", ex);
							continue;
						}
						if (fromAddress == null) continue;

						Connection[] connections = this.connections;
						for (int i = 0, n = connections.length; i < n; i++) {
							Connection connection = connections[i];
							if (fromAddress.equals(connection.udpRemoteAddress)) {
								fromConnection = connection;
								break;
							}
						}

						Object object;
						try {
							object = udp.readObject(fromConnection);
						} catch (DiDiNetException ex) {
							if (WARN) {
								if (fromConnection != null) {
									if (ERROR) error("hjh", "Error reading UDP from connection: " + fromConnection, ex);
								} else
									warn("hjh", "Error reading UDP from unregistered address: " + fromAddress, ex);
							}
							continue;
						}

						if (object instanceof FrameworkMessage) {
							if (object instanceof RegisterUDP) {
								// Store the fromAddress on the connection and reply over TCP with a RegisterUDP to indicate success.
								int fromConnectionID = ((RegisterUDP)object).connectionID;
								Connection connection = pendingConnections.remove(fromConnectionID);
								if (connection != null) {
									if (connection.udpRemoteAddress != null) continue outer;
									connection.udpRemoteAddress = fromAddress;
									addConnection(connection);
									connection.sendTCP(new RegisterUDP());
									if (DEBUG)
										debug("hjh", "Port " + udp.datagramChannel.socket().getLocalPort() + "/UDP connected to: "
											+ fromAddress);
									connection.notifyConnected();
									continue;
								}
								if (DEBUG)
									debug("hjh", "Ignoring incoming RegisterUDP with invalid connection ID: " + fromConnectionID);
								continue;
							}
							if (object instanceof DiscoverHost) {
								try {
									boolean responseSent = discoveryHandler
										.onDiscoverHost(udp.datagramChannel, fromAddress, serialization);
									if (DEBUG && responseSent) debug("hjh", "Responded to host discovery from: " + fromAddress);
								} catch (IOException ex) {
									if (WARN) warn("hjh", "Error replying to host discovery from: " + fromAddress, ex);
								}
								continue;
							}
						}

						if (fromConnection != null) {
							if (DEBUG) {
								String objectString = object == null ? "null" : object.getClass().getSimpleName();
								if (object instanceof FrameworkMessage) {
									if (TRACE) trace("hjh", fromConnection + " received UDP: " + objectString);
								} else
									debug("hjh", fromConnection + " received UDP: " + objectString);
							}
							fromConnection.notifyReceived(object);
							continue;
						}
						if (DEBUG) debug("hjh", "Ignoring UDP from unregistered address: " + fromAddress);
					} catch (CancelledKeyException ex) {
						if (fromConnection != null)
							fromConnection.close();
						else
							selectionKey.channel().close();
					}
				}
			}
		}
		long time = System.currentTimeMillis();
		Connection[] connections = this.connections;
		for (int i = 0, n = connections.length; i < n; i++) {
			Connection connection = connections[i];
			if (connection.tcp.isTimedOut(time)) {
				if (DEBUG) debug("hjh", connection + " timed out.");
				connection.close();
			} else {
				if (connection.tcp.needsKeepAlive(time)) connection.sendTCP(FrameworkMessage.keepAlive);
			}
			if (connection.isIdle()) connection.notifyIdle();
		}
	}

	private void keepAlive () {
		long time = System.currentTimeMillis();
		Connection[] connections = this.connections;
		for (int i = 0, n = connections.length; i < n; i++) {
			Connection connection = connections[i];
			if (connection.tcp.needsKeepAlive(time)) connection.sendTCP(FrameworkMessage.keepAlive);
		}
	}

	public void run () {
		if (TRACE) trace("hjh", "Server thread started.");
		shutdown = false;
		while (!shutdown) {
			try {
				update(250);
			} catch (IOException ex) {
				if (ERROR) error("hjh", "Error updating server connections.", ex);
				close();
			}
		}
		if (TRACE) trace("hjh", "Server thread stopped.");
	}

	public void start () {
		new Thread(this, "Server").start();
	}

	public void stop () {
		if (shutdown) return;
		close();
		if (TRACE) trace("hjh", "Server thread stopping.");
		shutdown = true;
	}

	private void acceptOperation (SocketChannel socketChannel) {
		Connection connection = newConnection();
		connection.initialize(serialization, writeBufferSize, objectBufferSize);
		connection.endPoint = this;
		connection.setClientChannel(socketChannel);
		UdpConnection udp = this.udp;
		if (udp != null) connection.udp = udp;
		try {
			SelectionKey selectionKey = connection.tcp.accept(selector, socketChannel);
			selectionKey.attach(connection);

			int id = nextConnectionID++;
			if (nextConnectionID == -1) nextConnectionID = 1;
			connection.id = id;
			connection.setConnected(true);
			connection.addListener(dispatchListener);

			if (udp == null)
				addConnection(connection);
			else
				pendingConnections.put(id, connection);

			RegisterTCP registerConnection = new RegisterTCP();
			registerConnection.connectionID = id;
			connection.sendTCP(registerConnection);

			if (udp == null) connection.notifyConnected();
		} catch (IOException ex) {
			connection.close();
			if (DEBUG) debug("hjh", "Unable to accept TCP connection.", ex);
		}
	}

	/** Allows the connections used by the server to be subclassed. This can be useful for storage per connection without an
	 * additional lookup. */
	protected Connection newConnection () {
		return new Connection();
	}

	private void addConnection (Connection connection) {
		Connection[] newConnections = new Connection[connections.length + 1];
		newConnections[0] = connection;
		System.arraycopy(connections, 0, newConnections, 1, connections.length);
		connections = newConnections;
	}

	void removeConnection (Connection connection) {
		ArrayList<Connection> temp = new ArrayList(Arrays.asList(connections));
		temp.remove(connection);
		connections = temp.toArray(new Connection[temp.size()]);

		pendingConnections.remove(connection.id);
	}

	//在访问量大时可能会数据错乱
	public void handleMessage(Connection connection,SocketMessage message){
		if(message == null)return;
		int length = 0;
		if(message.getFromType() == SocketFromType.PERSONAL){
			
		}else if (message.getFromType() == SocketFromType.GROUP) {
			
		}else if (message.getFromType() == SocketFromType.SYSTEM) {
			if(message.getPresence() == SocketPresence.LOCATION){//客户端请求周围商家定位消息
				User user = gson.fromJson(gson.toJson(message.getSender()),User.class);
				List<Shop> shops = service.findShopByLocation(user.getLatitude(), user.getLongtitude(),user.getShop().getType());
				message.setReceiver(shops);
				length = connection.sendTCP(gson.toJson(message));
				System.out.println("SocketPresence.LOCATION："+length);
			}else if (message.getPresence() == SocketPresence.BUY) {//群发购物消息
				User user = gson.fromJson(gson.toJson(message.getSender()),User.class);
				List<Shop> shops = service.findShopByLocation(user.getLatitude(), user.getLongtitude(),user.getShop().getType());
				StringBuffer buffer = new StringBuffer("&");
				for(int index = 0; index < shops.size();index++){
					if(shops.get(index).getShopOwnerId() == user.getUserId())continue;//排除自己
					buffer.append(shops.get(index).getShopId() + "&");//商家id以&id&拼接，便于查询
				}
				String uuid = UUID.randomUUID().toString();
				service.insertShopUndoMsg(buffer.toString(),user.getUserId(),message.getData(),uuid,user.getUserName(),user.getUserAddress());
				message.setUuid(uuid);
				for(Shop shop : shops){
					sendToTCPByClientId(shop.getShopOwnerId(),gson.toJson(message));
				}
			}else if (message.getPresence() == SocketPresence.ONLINE) {
				User user = gson.fromJson(message.getSender().toString(),User.class);
				connection.setClientId(user.getUserId());
			}else if (message.getPresence() == SocketPresence.MSG_ROBER_APPLY) {//商家请求抢单
				User receiver = gson.fromJson(gson.toJson(message.getReceiver()), User.class);
				User sender = gson.fromJson(gson.toJson(message.getSender()), User.class);
				ShopUndoMsg msg = service.findUndoMsg(message.getUuid());
				if(msg.getMsgData() == null){//表明数据已经被删除,未查找到
					message.setPresence(SocketPresence.MSG_ROBER_FAILED);
					connection.sendTCP(gson.toJson(message));
				}else{//抢单成功
					service.deleteShopUndoMsg(message.getUuid());//删除数据或设置状态
					if(sender.getUserRole() == 1){//发送者为商家
						Shop shop = service.findShopByShopId(sender.getUserId());
						sender.setShop(shop);
						message.setSender(sender);
						service.insertOrder(0,0, System.currentTimeMillis(),receiver.getUserId(),shop.getShopOwnerId(),shop.getShopName());
						message.setPresence(SocketPresence.MSG_ROBER_SUCCESS);
						connection.sendTCP(gson.toJson(message));//发送给商家(抢单成功)
						sendToTCPByClientId(receiver.getUserId(),gson.toJson(message));//发送给用户
					}
				}
				
				
			}
		}
	}
	
	public void sendToAllTCP (Object object) {
		Connection[] connections = this.connections;
		for (int i = 0, n = connections.length; i < n; i++) {
			Connection connection = connections[i];
			connection.sendTCP(object);
		}
	}

	public void sendToAllExceptTCP (int connectionID, Object object) {
		Connection[] connections = this.connections;
		for (int i = 0, n = connections.length; i < n; i++) {
			Connection connection = connections[i];
			if (connection.id != connectionID) connection.sendTCP(object);
		}
	}
	
	public void sendToTCPByClientId (int userId,Object object) {
		Connection[] connections = this.connections;
		for (int i = 0, n = connections.length; i < n; i++) {
			Connection connection = connections[i];
			if (connection.getClientId() == userId) {//发送给指定在线商户,不能为自己的商店
				connection.sendTCP(object);
				break;
			}
		}
	}
	

	public void sendToTCP (int connectionID, Object object) {
		Connection[] connections = this.connections;
		for (int i = 0, n = connections.length; i < n; i++) {
			Connection connection = connections[i];
			if (connection.id == connectionID) {
				connection.sendTCP(object);
				break;
			}
		}
	}

	public void sendToAllUDP (Object object) {
		Connection[] connections = this.connections;
		for (int i = 0, n = connections.length; i < n; i++) {
			Connection connection = connections[i];
			connection.sendUDP(object);
		}
	}

	public void sendToAllExceptUDP (int connectionID, Object object) {
		Connection[] connections = this.connections;
		for (int i = 0, n = connections.length; i < n; i++) {
			Connection connection = connections[i];
			if (connection.id != connectionID) connection.sendUDP(object);
		}
	}

	public void sendToUDP (int connectionID, Object object) {
		Connection[] connections = this.connections;
		for (int i = 0, n = connections.length; i < n; i++) {
			Connection connection = connections[i];
			if (connection.id == connectionID) {
				connection.sendUDP(object);
				break;
			}
		}
	}

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
		if (TRACE) trace("hjh", "Server listener added: " + listener.getClass().getName());
	}

	public void removeListener (Listener listener) {
		if (listener == null) throw new IllegalArgumentException("listener cannot be null.");
		synchronized (listenerLock) {
			Listener[] listeners = this.listeners;
			int n = listeners.length;
			Listener[] newListeners = new Listener[n - 1];
			for (int i = 0, ii = 0; i < n; i++) {
				Listener copyListener = listeners[i];
				if (listener == copyListener) continue;
				if (ii == n - 1) return;
				newListeners[ii++] = copyListener;
			}
			this.listeners = newListeners;
		}
		if (TRACE) trace("hjh", "Server listener removed: " + listener.getClass().getName());
	}

	/** Closes all open connections and the server port(s). */
	public void close () {
		Connection[] connections = this.connections;
		if (INFO && connections.length > 0) info("hjh", "Closing server connections...");
		for (int i = 0, n = connections.length; i < n; i++)
			connections[i].close();
		connections = new Connection[0];

		ServerSocketChannel serverChannel = this.serverChannel;
		if (serverChannel != null) {
			try {
				serverChannel.close();
				if (INFO) info("hjh", "Server closed.");
			} catch (IOException ex) {
				if (DEBUG) debug("hjh", "Unable to close server.", ex);
			}
			this.serverChannel = null;
		}

		UdpConnection udp = this.udp;
		if (udp != null) {
			udp.close();
			this.udp = null;
		}

		synchronized (updateLock) { // Blocks to avoid a select while the selector is used to bind the server connection.
		}
		// Select one last time to complete closing the socket.
		selector.wakeup();
		try {
			selector.selectNow();
		} catch (IOException ignored) {
		}
	}

	/** Releases the resources used by this server, which may no longer be used. */
	public void dispose () throws IOException {
		close();
		selector.close();
	}

	public Thread getUpdateThread () {
		return updateThread;
	}

	/** Returns the current connections. The array returned should not be modified. */
	public Connection[] getConnections () {
		return connections;
	}
}
