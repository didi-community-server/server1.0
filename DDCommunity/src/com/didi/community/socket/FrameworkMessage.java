
package com.didi.community.socket;


public interface FrameworkMessage {
	static final FrameworkMessage.KeepAlive keepAlive = new KeepAlive();

	/** Internal message to give the client the server assigned connection ID. */
	static public class RegisterTCP implements FrameworkMessage {
		public int connectionID;
	}

	/** Internal message to give the server the client's UDP port. */
	static public class RegisterUDP implements FrameworkMessage {
		public int connectionID;
	}

	/** Internal message to keep connections alive. */
	static public class KeepAlive implements FrameworkMessage {
	}

	/** Internal message to discover running servers. */
	static public class DiscoverHost implements FrameworkMessage {
	}

	/** Internal message to determine round trip time. */
	static public class Ping implements FrameworkMessage {
		public int id;
		public boolean isReply;
	}
}
