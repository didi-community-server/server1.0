
package com.didi.community.socket;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.hjh.request.bean.SocketMessage;

import com.didi.community.socket.Connection;
import com.didi.community.socket.Listener;
import com.didi.community.socket.Log;
import com.didi.community.socket.Server;
import com.google.gson.Gson;


public class ChatServer {
	private Server server;
	private Gson gson = new Gson();

	public ChatServer () throws IOException {
		Log.set(Log.LEVEL_DEBUG);
		server = new Server(gson);


		server.addListener(new Listener() {
			public void received (Connection c, Object object) {
				String json = object.toString();
				if(!json.equals("didi-client-keepAlive")){
					server.handleMessage(c, gson.fromJson(json, SocketMessage.class));
				}
			}

			public void disconnected (Connection c) {
				
			}
		});
		server.bind(54555);
		server.start();

		// Open a window to provide an easy way to stop the server.
		JFrame frame = new JFrame("Chat Server");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed (WindowEvent evt) {
				server.stop();
			}
		});
		frame.getContentPane().add(new JLabel("Close to stop the chat server."));
		frame.setSize(320, 200);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	void updateNames () {
		
	}

	public void stop(){
		server.stop();
	}

}
