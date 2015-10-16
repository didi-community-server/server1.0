package com.didi.community.bean;

import java.io.Serializable;

/**
 * 订单
 * @author hjh
 * 2015-10-4下午1:47:59
 */
public class Order implements Serializable{
	private static final long serialVersionUID = -7410400194580649516L;

	private int id;
	private int userStatus;//订单当前状态，0：待付款，1：待收货，2：待评论(表示已收货)，3：退货，4：已完成
	private int shopStatus;//商家订单状态，0：未收款，1：未发货，2：已完成，3：退货单
	private long startTime;
	private long endTime;
	private int userId;
	private int shopId;
	private String shopName;
	
	public Order(){
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public int getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(int userStatus) {
		this.userStatus = userStatus;
	}

	public int getShopStatus() {
		return shopStatus;
	}

	public void setShopStatus(int shopStatus) {
		this.shopStatus = shopStatus;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	
	
	
}
