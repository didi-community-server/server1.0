package com.didi.community.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hjh.request.bean.JsonParamter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.didi.community.bean.Order;
import com.didi.community.bean.ResponseJson;
import com.didi.community.bean.ResponseListJson;
import com.didi.community.bean.ResponseOrder;
import com.didi.community.bean.User;
import com.didi.community.request.paramter.OrderParamters;
import com.google.gson.Gson;

public class OrderController extends BaseController{

	@Override
	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse response) throws Exception {
		out = response.getWriter();
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
	
	private void handleJsonRequest(String json){
		JsonParamter paramter = gson.fromJson(json, JsonParamter.class);
		OrderParamters jsonObject = gson.fromJson(gson.toJson(paramter.getRequestParamter()), OrderParamters.class);
		switch (paramter.getRequestType()) {
		case 1://查询
			List<Order> orders = service.findOrderById(jsonObject.isUser(),jsonObject.getId(),jsonObject.getStatus());
			int total = service.findTotalOrderCount(jsonObject);
			ResponseOrder responseOrder = new ResponseOrder();
			responseOrder.setOrders(orders);
			responseOrder.setCurrentPage(jsonObject.getCurentPage());
			responseOrder.setTotalPage(total % jsonObject.getPageSize() == 0 ? total / jsonObject.getPageSize() : (total / jsonObject.getPageSize() +1));
			responseOrder.setHasMore(responseOrder.getTotalPage() > jsonObject.getCurentPage());
			ResponseJson<ResponseOrder> resp = new ResponseJson<ResponseOrder>();
			resp.setStatus(1);
			resp.setInfo("用户订单");
			resp.setData(responseOrder);
			
			out.println(new Gson().toJson(resp));
			out.flush();
			out.close();
			break;
		case 2:
			
			break;
		default:
			break;
		}
	}

}
