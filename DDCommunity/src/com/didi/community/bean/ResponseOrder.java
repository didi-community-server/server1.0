package com.didi.community.bean;

import java.io.Serializable;
import java.util.List;

public class ResponseOrder implements Serializable{

	private static final long serialVersionUID = 8121539028342689388L;

	private int totalPage;
	private int currentPage;
	private boolean hasMore;
	private List<Order> orders;
	
	public ResponseOrder(){
		
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public boolean isHasMore() {
		return hasMore;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}
	
}
