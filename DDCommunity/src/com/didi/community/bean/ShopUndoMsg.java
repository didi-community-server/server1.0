package com.didi.community.bean;

import java.io.Serializable;

/**
 * 商家可抢单
 * @author hjh
 * 2015-9-26下午3:39:28
 */
public class ShopUndoMsg implements Serializable{

	
	private static final long serialVersionUID = 3668537937593102622L;
	private int msgId;
	private int userId;
	private String msgData;
	private long msgTime;
	private int msgType;
	private String uuid;
	private String userName;
	private String userAddress;
	
	public ShopUndoMsg(){
		setMsgData(null);
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getMsgData() {
		return msgData;
	}

	public void setMsgData(String msgData) {
		this.msgData = msgData;
	}

	public long getMsgTime() {
		return msgTime;
	}

	public void setMsgTime(long msgTime) {
		this.msgTime = msgTime;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}
	
	
}
