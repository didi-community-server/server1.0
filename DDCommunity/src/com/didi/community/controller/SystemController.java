package com.didi.community.controller;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.hjh.request.bean.JsonParamter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import sun.management.resources.agent;

import com.didi.community.bean.FormParamter;
import com.didi.community.bean.ResponseJson;
import com.didi.community.bean.ResponseListJson;
import com.didi.community.bean.Shop;
import com.didi.community.bean.ShopUndoMsg;
import com.didi.community.bean.User;
import com.didi.community.service.UserService;
import com.google.gson.Gson;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class SystemController extends BaseController{

	
	@Override
	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
		
		
		FormParamter paramter = new FormParamter();
		doRequest(arg0, paramter);
		
		if(paramter.getType() == null){//未收到表单,表示为json
			StringBuilder builder = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(arg0.getInputStream(),"utf-8"));  
	        String temp;  
	        while ((temp = br.readLine()) != null) {  
	            builder.append(temp);  
	        }  
	        
	        br.close();
	        temp = builder.toString();
	        handleJsonRequest(temp);
	        return null;
		}
		
		PrintWriter out = arg1.getWriter();
		
		if(paramter.getType().equals("shop_can_rob_order")){//商家获取自己可抢单消息
			String shopId = paramter.getParamters().get("shop_id");
			List<ShopUndoMsg> list = service.findUndoMsgs(shopId);
			ResponseListJson<ShopUndoMsg> response = new ResponseListJson<ShopUndoMsg>();
			response.setInfo("商家可抢单数据");
			response.setStatus(1);
			response.setData(list);
			
			out.println(new Gson().toJson(response));
			out.flush();
			out.close();
		}else if(paramter.getType().equals("shop_query_info")){//商户获取自己的信息
			String userId = paramter.getParamters().get("user_id");
			Shop shop = service.findShopByUserId(Integer.parseInt(userId));
			List<ShopUndoMsg> list = service.findUndoMsgs(shop.getShopId()+"");
			if(list == null){
				shop.setCanRobCount(0);
			}else {
				shop.setCanRobCount(list.size());
			}
			
			ResponseJson<Shop> response = new ResponseJson<Shop>();
			response.setInfo("商家数据");
			response.setStatus(1);
			response.setData(shop);
			
			out.println(new Gson().toJson(response));
			out.flush();
			out.close();
		}
		
		return null;
	}
	
	private void handleJsonRequest(String json){
		JsonParamter paramter = gson.fromJson(json, JsonParamter.class);
		paramter.getRequestType();
	}
	
	private void doRequest(HttpServletRequest request,FormParamter paramter){
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
				while (i.hasNext()) {
					FileItem item = (FileItem) i.next(); 
					if (item.isFormField()) {
						String paramName = item.getFieldName();
						String paramValue = item.getString();
						if("request_type".equals(paramName)){
							paramter.setType(paramValue);
						}else {
							paramter.putParamter(paramName, paramValue);
						}
					}else {//文件
						 // 上传表单：得到输入流，处理上传：保存到服务器的某个目录中，保存时的文件名是啥？
				          String fileName = item.getName();// 得到上传文件的名称
				          //解决用户没有选择文件上传的情况
				          if(fileName==null||fileName.trim().equals("")){
				            continue;
				          }
				          fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
				          String newFileName = UUID.randomUUID().toString() + "_" + fileName;
				          System.out.println("上传的文件名是：" + fileName);
				          InputStream in = item.getInputStream();
				          String savePath = "";
				          OutputStream out = new FileOutputStream(savePath);
				          byte b[] = new byte[1024];
				          int len = -1;
				          while ((len = in.read(b)) != -1) {
				            out.write(b, 0, len);
				          }
				          in.close();
				          out.close();
				          item.delete();//删除临时文件
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
