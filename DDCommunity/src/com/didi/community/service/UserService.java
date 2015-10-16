package com.didi.community.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didi.community.bean.BankCard;
import com.didi.community.bean.LoginLog;
import com.didi.community.bean.Order;
import com.didi.community.bean.Shop;
import com.didi.community.bean.ShopUndoMsg;
import com.didi.community.bean.User;
import com.didi.community.dao.LoginLogDao;
import com.didi.community.dao.OrderDao;
import com.didi.community.dao.UserDao;
import com.didi.community.request.paramter.OrderParamters;

public class UserService {
	private UserDao userDao;
	private LoginLogDao loginLogDao;
	private OrderDao orderDao;
	
	

	public OrderDao getOrderDao() {
		return orderDao;
	}

	public void setOrderDao(OrderDao orderDao) {
		this.orderDao = orderDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public LoginLogDao getLoginLogDao() {
		return loginLogDao;
	}

	public void setLoginLogDao(LoginLogDao loginLogDao) {
		this.loginLogDao = loginLogDao;
	}

	public boolean hasMatchUser(String userName, String password) {
		int matchCount = userDao.getMatchCount(userName, password);
		return matchCount > 0;
	}

	public User findUserByUserName(String userName) {
		return userDao.findUserByUserName(userName);
	}

	public void loginSucess(User user) {
		user.setCredits(5 + user.getCredits());
		LoginLog loginLog = new LoginLog();
		loginLog.setUserId(user.getUserId());
		loginLog.setIp(user.getLastLoginIp());
		loginLog.setLoginTime(user.getLastLoginTime());
		userDao.updateLoginInfo(user);
		loginLogDao.insertLoginLog(loginLog);
	}
	
	public User register(String username,String password,String nick,String phone,String ip){
		return userDao.insertUser(username, password,nick,phone,ip);
	}
	
	/**
	 *认证 
	 * @param shop
	 * @param card
	 */
	public void authenty(Shop shop,BankCard card){
		userDao.shopAuthenty(shop.getShopOwner(),shop.getShopOwnerIdcrad(),shop.getShopPhone(),
				shop.getShopQQ(),card.getCard(),card.getOwner(),card.getAdr(),shop.getShopName(),
				shop.getShopAdr(),shop.getShopWorkTime(),shop.getShopSaleType(),
				shop.getShopBusinessLiscence(),shop.getShopIcon(),shop.getShopOwnerId(),shop.getType());
	}
	//根据用户id取出相应的商家信息
	public Shop findShopByUserId(int userId){
		return userDao.findShopByUserId(userId);
	}
	
	public Shop findShopByShopId(int shopId){
		return userDao.findShopByShopId(shopId);
	}
	
	public List<Shop> findShopByLocation(double latitude,double longtitude,int type){
		return userDao.findShopByLocation(latitude, longtitude,type);
	}
	
	//用户发单时，保存消息，在商户接收后删除
	public void insertShopUndoMsg(String shopsId,int userId,String data,String uuid,String name,String adr){
		userDao.insertShopUndoMsg(shopsId, userId, data,uuid,name,adr);
	}
	
	public ShopUndoMsg findUndoMsg(String uuid){
		return userDao.findUndoMsg(uuid);
	}
	
	public void deleteShopUndoMsg(String uuid){
		userDao.deleteShopUndoMsg(uuid);
	}
	
	//商家查询自己可以抢单的数据
	public List<ShopUndoMsg> findUndoMsgs(String shopId){
		return userDao.findUndoMsgs(shopId);
	}
	
	public void insertOrder(int userStatus,int shopStatus,long startTime,int userId,int shopId,String shopname){
		orderDao.insertOrder(userStatus,shopStatus, startTime, userId, shopId,shopname);
	}
	
	public List<Order> findOrderById(boolean user,int userId,int status){
		return orderDao.findOrderById(user, userId, status);
	}
	
	public int findTotalOrderCount(OrderParamters jsonObject){
		return orderDao.findTotalOrderCount(jsonObject.isUser(),jsonObject.getId(),jsonObject.getStatus());
	}
	
}
