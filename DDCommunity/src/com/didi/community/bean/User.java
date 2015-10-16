package com.didi.community.bean;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable{
	private static final long serialVersionUID = -2240252680350204069L;
	
	private int userId;
	private String userName;
	private String userNick;
	private String userPhone;
	private String userEmail;
	private String userAddress;
	private String realName;
	private String zip;//
	private String password;
	private int credits;//
	private Date registeTime;
	private Date lastLoginTime;
	private String lastLoginAdr;
	private String lastLoginIp;
	private int userRole;//用户当前角色 0:用户，1：商户，2：群组
	private int shopAuthentyStatus;//商家认证状态
	private int propertyAuthentyStatus;//物业认证状态
	private double latitude;//
    private double longtitude;//
    private Shop shop;//
	
	public User(){
		
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserNick() {
		return userNick;
	}

	public void setUserNick(String userNick) {
		this.userNick = userNick;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getCredits() {
		return credits;
	}

	public void setCredits(int credits) {
		this.credits = credits;
	}

	public Date getRegisteTime() {
		return registeTime;
	}

	public void setRegisteTime(Date registeTime) {
		this.registeTime = registeTime;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getLastLoginAdr() {
		return lastLoginAdr;
	}

	public void setLastLoginAdr(String lastLoginAdr) {
		this.lastLoginAdr = lastLoginAdr;
	}

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	public int getUserRole() {
		return userRole;
	}

	public void setUserRole(int userRole) {
		this.userRole = userRole;
	}

	public int getShopAuthentyStatus() {
		return shopAuthentyStatus;
	}

	public void setShopAuthentyStatus(int shopAuthentyStatus) {
		this.shopAuthentyStatus = shopAuthentyStatus;
	}

	public int getPropertyAuthentyStatus() {
		return propertyAuthentyStatus;
	}

	public void setPropertyAuthentyStatus(int propertyAuthentyStatus) {
		this.propertyAuthentyStatus = propertyAuthentyStatus;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

	
}
