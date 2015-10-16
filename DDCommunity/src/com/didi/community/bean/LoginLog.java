package com.didi.community.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * µÇÂ¼ÈÕÖ¾
 * @author hjh
 * 2015-9-20ÉÏÎç12:09:16
 */
public class LoginLog implements Serializable{

	
	private static final long serialVersionUID = -2357372407660962809L;

	private int logId;
	private int userId;
	private String ip;
	private Date loginTime;
	
	public LoginLog(){
		
	}

	public int getLogId() {
		return logId;
	}

	public void setLogId(int logId) {
		this.logId = logId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}
	
	
	
}
