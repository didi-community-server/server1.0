package com.didi.community.controller;

import java.io.PrintWriter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.mvc.Controller;

import com.didi.community.service.UserService;
import com.google.gson.Gson;

public abstract class BaseController implements Controller{

	protected Gson gson = new Gson();
	protected ApplicationContext factory = new ClassPathXmlApplicationContext("applicationContext.xml");
	protected UserService service = (UserService) factory.getBean("userService");
	protected PrintWriter out;
}
