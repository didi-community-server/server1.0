package com.didi.community.controller;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.didi.community.bean.BankCard;
import com.didi.community.bean.ResponseJson;
import com.didi.community.bean.Shop;
import com.didi.community.bean.User;
import com.didi.community.service.UserService;


public class AuthentyController extends BaseController{

	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Shop temp = new Shop();
		BankCard card = new BankCard();
		PrintWriter out = response.getWriter();
		if(doRequest(request,temp,card)){
			service.authenty(temp,card);
			ResponseJson<Shop> resp = new ResponseJson<Shop>();
			resp.setStatus(1);
			resp.setInfo("认证中...");
			resp.setData(temp);
			
			out.println(resp.toJson(Shop.class));
			out.flush();
			out.close();
		}else {
			ResponseJson<Shop> resp = new ResponseJson<Shop>();
			resp.setStatus(0);
			resp.setInfo("认证失败");
			resp.setData(null);
			
			out.println(resp.toJson(Shop.class));
			out.flush();
			out.close();
		}
		
		return null;
	}
	
	private boolean doRequest(HttpServletRequest request,Shop shop,BankCard card){
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
					String paramName = item.getFieldName();
					if (item.isFormField()) {
						
						String paramValue = item.getString();
						if("shop_owner".equals(paramName)){//
							shop.setShopOwner(paramValue);
						}else if ("shop_owner_idcard".equals(paramName)) {
							shop.setShopOwnerIdcrad(paramValue);
						}else if ("shop_phone".equals(paramName)) {
							shop.setShopPhone(paramValue);
						}else if ("shop_qq".equals(paramName)) {
							shop.setShopQQ(paramValue);
						}else if("shop_name".equals(paramName)){
							shop.setShopName(paramValue);
						}else if("shop_adr".equals(paramName)){
							shop.setShopAdr(paramValue);
						}else if("shop_work_time".equals(paramName)){
							shop.setShopWorkTime(paramValue);
						}else if("shop_sale_type".equals(paramName)){
							shop.setShopSaleType(paramValue);
						}else if("shop_owner_id".equals(paramName)){
							shop.setShopOwnerId(Integer.parseInt(paramValue));
						}else if("shop_bank_card".equals(paramName)){
							card.setCard(paramValue);
						}else if("shop_bank_adr".equals(paramName)){
							card.setAdr(paramValue);
						}else if("shop_bank_owner".equals(paramName)){
							card.setOwner(paramValue);
						}else if("type".equals(paramName)){
							shop.setType(Integer.parseInt(paramValue));
						}
					}else {
						byte[] data = item.get();
                        String filePath = request.getSession().getServletContext().getRealPath("/ImagesUpload") + "/" + item.getName();
                        FileOutputStream fos = new FileOutputStream(filePath);
                        fos.write(data);
                        fos.close();
                        filePath = "http://www.jinhuicc.com:24103/DDCommunity/ImagesUpload/"+ item.getName();
                        if("shop_licence".equals(paramName)){
                        	shop.setShopBusinessLiscence(filePath);
                        }else if(paramName.startsWith("files")){
							shop.setShopIcon(shop.getShopIcon()+filePath+";");
						}
					}
				}
				return true;
			} catch (FileUploadBase.FileSizeLimitExceededException e) {//单个文件超出最大异常
				e.printStackTrace();
				return false;
			} catch (FileUploadBase.SizeLimitExceededException e) {//总文件大小异常
				return false;
			} catch (Exception e) {
				return false;
			}
		}
		return isHaveData;
	}


}
