package com.didi.community.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.didi.community.bean.Order;

public class OrderDao {

	private JdbcTemplate jdbcTemplate;
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	//获取数量
	public int findTotalOrderCount(boolean user,int userId,int status){
		String selectArgs = "";
		if(user){
			selectArgs = "order_user_status=? and order_user_id=?";
		}else {
			selectArgs = "order_shop_status=? and order_shop_id=?";
		}
		
		String sqlStr = "select count(*) from app_order where "+selectArgs;
		return jdbcTemplate.queryForInt(sqlStr, new Object[]{status,userId});
	}
	
	//保存订单
	public void insertOrder(int userStatus,int shopStatus,long startTime,int userId,int shopId,String shopname){
		String sqlStr = "insert into app_order(order_user_status,order_shop_status,order_start_time,order_user_id,order_shop_id,order_shop_name) values(?,?,?,?,?,?)";
		jdbcTemplate.update(sqlStr, new Object[]{userStatus,shopStatus,startTime,userId,shopId,shopname});
	}
	
	//根据用户id和类型取出订单
	public List<Order> findOrderById(boolean user,int userId,int status){
		String selectArgs = "";
		if(user){
			selectArgs = "order_user_status=? and order_user_id=?";
		}else {
			selectArgs = "order_shop_status=? and order_shop_id=?";
		}
		
		String sqlStr = "select * from app_order where "+selectArgs;
		List<Order> orders = jdbcTemplate.query(sqlStr,new RowMapper<Order>() {

			@Override
			public Order mapRow(ResultSet rs, int arg1) throws SQLException {
				Order order = new Order();
				order.setId(rs.getInt("order_id"));
				order.setEndTime(rs.getLong("order_end_time"));
				order.setStartTime(rs.getLong("order_start_time"));
				order.setShopId(rs.getInt("order_shop_id"));
				order.setUserId(rs.getInt("order_user_id"));
				order.setUserStatus(rs.getInt("order_user_status"));
				order.setShopStatus(rs.getInt("order_shop_status"));
				order.setShopName(rs.getString("order_shop_name"));
				return order;
			}
		},new Object[]{status,userId});
		
		return orders;
	}
}
