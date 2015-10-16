package com.didi.community.controller;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

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

public class RegisteController extends BaseController{

	@Override
	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
//		String name = arg0.getParameter("user_name");
//		String password = arg0.getParameter("user_password");
//		String phone = arg0.getParameter("user_phone");
//		String nick = arg0.getParameter("user_nick");
//		String code = arg0.getParameter("user_code");
		
		User temp = new User();
		doRequest(arg0, temp);
		
		PrintWriter out = arg1.getWriter();
		if(service.findUserByUserName(temp.getUserName()).getUserId() != 0){//是否存在该用户
			ResponseJson<User> resp = new ResponseJson<User>();
			resp.setStatus(0);
			resp.setInfo("该账户已注册");
			resp.setData(null);
			
			out.println(resp.toJson(User.class));
			out.flush();
			out.close();
		}else {
			User user= service.register(temp.getUserName(),temp.getPassword(),temp.getUserNick(),
					temp.getUserPhone(),arg0.getRemoteAddr());
			ResponseJson<User> resp = new ResponseJson<User>();
			resp.setData(user);
			if(user == null){
				resp.setStatus(0);
				resp.setInfo("注册失败");
			}else {
				resp.setStatus(1);
				resp.setInfo("注册成功");
			}
			
			out.println(resp.toJson(User.class));
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
				factory.setSizeThreshold(4096);//设置缓冲区大小，这里是4kb  
				//用以上工厂实例化上传组件
				ServletFileUpload upload = new ServletFileUpload(factory); 
				upload.setSizeMax(3*1024*1024);//设置最大文件尺寸，这里是3MB 
				List<FileItem> items = upload.parseRequest(request);//得到所有的文件 
				Iterator<FileItem> i = items.iterator(); 
				String type = "";
				while (i.hasNext()) {
					FileItem item = (FileItem) i.next(); 
					if (item.isFormField()) {
						String paramName = item.getFieldName();
						String paramValue = item.getString();
						if("user_name".equals(paramName)){//账户
							user.setUserName(paramValue);
						}else if ("user_password".equals(paramName)) {
							user.setPassword(paramValue);
						}else if ("user_nick".equals(paramName)) {
							user.setUserNick(paramValue);
						}else if ("user_phone".equals(paramName)) {
							user.setUserPhone(paramValue);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
