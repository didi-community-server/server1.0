package com.didi.community.dao;

import org.springframework.jdbc.core.JdbcTemplate;

import com.didi.community.bean.LoginLog;

public class LoginLogDao {

	private JdbcTemplate jdbcTemplate;

	public void insertLoginLog(LoginLog loginLog) {
		String sqlStr = "insert into login_log(user_id,ip,login_datetime) values(?,?,?)";
		Object[] args = { loginLog.getUserId(), loginLog.getIp(),
				loginLog.getLoginTime()};
		jdbcTemplate.update(sqlStr, args);
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	

}
