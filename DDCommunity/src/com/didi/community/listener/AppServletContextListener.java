package com.didi.community.listener;

import java.net.ServerSocket;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.didi.community.socket.ChatServer;


public class AppServletContextListener implements ServletContextListener{
	ChatServer server;
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try{
			if(server != null){
				server.stop();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.print("DDCommunity init success.");
		try{
			server = new ChatServer();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
