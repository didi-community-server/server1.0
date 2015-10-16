package com.didi.community.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.didi.community.bean.ResponseJson;
import com.didi.community.bean.User;
import com.didi.community.service.UserService;
import com.google.gson.Gson;

public class LoginController extends BaseController{

	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		User temp = new User();
		doRequest(request,temp);
		
		PrintWriter out = response.getWriter();
		if(!service.hasMatchUser(temp.getUserName(),temp.getPassword())){
			ResponseJson<User> resp = new ResponseJson<User>();
			resp.setStatus(0);
			resp.setInfo("用户名或密码错误");
			resp.setData(null);
			
			out.println(resp.toJson(User.class));
			out.flush();
			out.close();
		}else {
			User user = service.findUserByUserName(temp.getUserName());
			user.setLastLoginIp(request.getRemoteAddr());
			user.setLastLoginTime(new Date());
			service.loginSucess(user);
			
			ResponseJson<User> resp = new ResponseJson<User>();
			resp.setStatus(1);
			resp.setInfo("登录成功");
			if(user.getUserRole() == 2){//商户
				user.setShop(service.findShopByUserId(user.getUserId()));
			}
			
			resp.setData(user);
			String json = resp.toJson(User.class);//
			json = new Gson().toJson(resp);
			System.out.print(json);
			out.println(json);
			out.flush();
			out.close();
		}
		
		return null;
	}
	
	private void doRequest(HttpServletRequest request,User user){
		boolean isHaveData = ServletFileUpload.isMultipartContent(request);
		if(isHaveData){
			try {
				DiskFileItemFactory factory = new DiskFileItemFactory(); 
				factory.setSizeThreshold(4096);//
				
				ServletFileUpload upload = new ServletFileUpload(factory); 
				upload.setSizeMax(3*1024*1024);//
				List<FileItem> items = upload.parseRequest(request);
				Iterator<FileItem> i = items.iterator(); 
				String type = "";
				while (i.hasNext()) {
					FileItem item = (FileItem) i.next(); 
					if (item.isFormField()) {
						String paramName = item.getFieldName();
						String paramValue = item.getString();
						if("user_name".equals(paramName)){
							user.setUserName(paramValue);
						}else if ("user_password".equals(paramName)) {
							user.setPassword(paramValue);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
