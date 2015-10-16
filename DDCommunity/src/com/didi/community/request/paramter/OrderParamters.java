package com.didi.community.request.paramter;

import java.io.Serializable;

/**
 * 订单请求参数
 * @author hjh
 * 2015-10-4下午3:03:32
 */
public class OrderParamters implements Serializable{

	private static final long serialVersionUID = 3264153037980783055L;
	private boolean user;
	private int id;
	private int status;
	private int pageSize;
	private int curentPage;
	
	public OrderParamters(){
		
	}

	public boolean isUser() {
		return user;
	}

	public void setUser(boolean user) {
		this.user = user;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurentPage() {
		return curentPage;
	}

	public void setCurentPage(int curentPage) {
		this.curentPage = curentPage;
	}
	
}
