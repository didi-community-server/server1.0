package com.didi.community.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.didi.community.bean.Order;
import com.didi.community.bean.Shop;
import com.didi.community.bean.ShopUndoMsg;
import com.didi.community.bean.User;

/**
 * 用户操作
 * @author hjh
 * 2015-9-19上午10:44:50
 */
public class UserDao {
	private JdbcTemplate jdbcTemplate;
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public int getMatchCount(String userName, String password) {
		String sqlStr = "select count(*) from user where user_name=? and password=?";
		return jdbcTemplate.queryForInt(sqlStr, new Object[] { userName,
				password });
	}

	/**
	 * 根据用户名或电话取出用户信息
	 * @param userName
	 * @return
	 */
	public User findUserByUserName(final String userName) {
		String sqlStr = "select * from user where user_name=? or user_phone=?";
		final User user = new User();
		
		jdbcTemplate.query(sqlStr, new Object[] { userName,userName},
				new RowCallbackHandler() {

					public void processRow(ResultSet rs) throws SQLException {
						user.setUserId(rs.getInt("user_id"));
						user.setUserName(userName);
						user.setCredits(rs.getInt("credits"));
						user.setUserNick(rs.getString("user_nick"));
						user.setUserEmail(rs.getString("user_email"));
						user.setUserPhone(rs.getString("user_phone"));
						user.setRealName(rs.getString("realname"));
						user.setUserAddress(rs.getString("address"));
						user.setZip(rs.getString("zip"));
						user.setPassword(rs.getString("password"));
						user.setRegisteTime(rs.getDate("registe_time"));
						user.setLastLoginTime(rs.getDate("last_login_time"));
						user.setLastLoginAdr(rs.getString("last_login_adr"));
						user.setLastLoginIp(rs.getString("last_ip"));
						user.setUserRole(rs.getInt("user_current_role"));
						user.setShopAuthentyStatus(rs.getInt("shop_authenty_status"));
						user.setPropertyAuthentyStatus(rs.getInt("property_authenty_status"));
					}
				});

		return user;

	}

	public void updateLoginInfo(User user) {
		String sqlStr = "update user set last_login_adr=?,last_ip=?,last_login_time=?,credits=? where user_id=?";
		jdbcTemplate.update(sqlStr,
				new Object[] { user.getLastLoginAdr(), user.getLastLoginIp(),
						user.getLastLoginTime(),user.getCredits(), user.getUserId() });
	}

	public User insertUser(String username, String password,String nick,String phone,String ip) {
		String sqlStr = "insert into user(user_name,password,user_nick,user_phone,last_ip,registe_time,last_login_time) values(?,?,?,?,?,?,?)";
		jdbcTemplate.update(sqlStr, new Object[] { username, password,nick,phone,ip,new Date(),new Date()});
		return findUserByUserName(username);
	}
	
	/**
	 * 商家认证
	 */
	public void shopAuthenty(String owner,String ownerIdcard,String phone,String qq,
			String bankCrad,String bankOwner,String bankAdr,String shopName,String shopAdr,
			String workTime,String saleType,String licence,String icons,int userId,int type){
		String sqlShop = "insert into shop(shop_owner,shop_owner_idcard,shop_phone,shop_qq," +
				"shop_name,shop_adr,shop_work_time,shop_sale_type,shop_business_licence," +
				"shop_icon,shop_owner_id,type) values(?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(sqlShop,new Object[]{owner,ownerIdcard,phone,qq,shopName,shopAdr,
				workTime,saleType,licence,icons,userId,type});
		
		String sqlBank = "insert into shop_bank(bank_owner,bank_card,bank_adr,bank_shop_id) values" +
				"(?,?,?,?)";
		jdbcTemplate.update(sqlBank, new Object[]{bankOwner,bankCrad,bankAdr,findShopByShopName(userId, shopName).getShopId()});
	
	}
	
	public Shop findShopByShopName(int userId,String shopName){
		String sqlStr = "select * from shop where shop_owner_id=? and shop_name=?";
		final Shop shop = new Shop();
		jdbcTemplate.query(sqlStr, new Object[] { userId,shopName},
				new RowCallbackHandler() {

					@Override
					public void processRow(ResultSet rs) throws SQLException {
						shop.setShopId(rs.getInt("shop_id"));
					}
			
		});
		
		return shop;
	}
	
	//根据商家id取出相应的商家信息
		public Shop findShopByShopId(int shopId){
			String sqlStr = "select * from shop where shop_id=?";
			final Shop shop = new Shop();
			jdbcTemplate.query(sqlStr, new Object[] { shopId},
					new RowCallbackHandler() {

						@Override
						public void processRow(ResultSet rs) throws SQLException {
							shop.setShopId(rs.getInt("shop_id"));
							shop.setShopOwner(rs.getString("shop_owner"));
							shop.setShopOwnerIdcrad(rs.getString("shop_owner_idcard"));
							shop.setShopPhone(rs.getString("shop_phone"));
							shop.setShopQQ(rs.getString("shop_qq"));
							shop.setShopName(rs.getString("shop_name"));
							shop.setShopAdr(rs.getString("shop_adr"));
							shop.setShopWorkTime(rs.getString("shop_work_time"));
							shop.setShopSaleType(rs.getString("shop_sale_type"));
							shop.setShopBusinessLiscence(rs.getString("shop_business_licence"));
							shop.setShopIcon(rs.getString("shop_icon"));
							shop.setShopAuthentyStatus(rs.getInt("shop_authenty_status"));
							shop.setShopOwnerId(rs.getInt("shop_owner_id"));
							shop.setLatitude(rs.getDouble("latitude"));
							shop.setLongtitude(rs.getDouble("longtitude"));
							shop.setType(rs.getInt("type"));
						}
				
			});
			
			return shop;
		}
	
	//根据用户id取出相应的商家信息
	public Shop findShopByUserId(int userId){
		String sqlStr = "select * from shop where shop_owner_id=?";
		final Shop shop = new Shop();
		jdbcTemplate.query(sqlStr, new Object[] { userId},
				new RowCallbackHandler() {

					@Override
					public void processRow(ResultSet rs) throws SQLException {
						shop.setShopId(rs.getInt("shop_id"));
						shop.setShopOwner(rs.getString("shop_owner"));
						shop.setShopOwnerIdcrad(rs.getString("shop_owner_idcard"));
						shop.setShopPhone(rs.getString("shop_phone"));
						shop.setShopQQ(rs.getString("shop_qq"));
						shop.setShopName(rs.getString("shop_name"));
						shop.setShopAdr(rs.getString("shop_adr"));
						shop.setShopWorkTime(rs.getString("shop_work_time"));
						shop.setShopSaleType(rs.getString("shop_sale_type"));
						shop.setShopBusinessLiscence(rs.getString("shop_business_licence"));
						shop.setShopIcon(rs.getString("shop_icon"));
						shop.setShopAuthentyStatus(rs.getInt("shop_authenty_status"));
						shop.setShopOwnerId(rs.getInt("shop_owner_id"));
						shop.setLatitude(rs.getDouble("latitude"));
						shop.setLongtitude(rs.getDouble("longtitude"));
						shop.setType(rs.getInt("type"));
					}
			
		});
		
		return shop;
	}
	
	/**
	 * 用户根据经纬度取出商铺数据
	 * @param latitude
	 * @param longtitude
	 * @param type 商家类型
	 * @return
	 */
	public List<Shop> findShopByLocation(double latitude,double longtitude,int type){
		String sqlStr = "select * from shop where type=? and latitude>? and latitude<? and longtitude>? and longtitude <?" +
				" order by abs(longtitude -?)+abs(latitude -?) limit 10";
//		jdbcTemplate.queryForList(sqlStr, new Object[]{latitude-1,latitude+1,longtitude-1,longtitude+1,longtitude,latitude});
		List<Shop> shops = jdbcTemplate.query(sqlStr, new RowMapper<Shop>(){

			@Override
			public Shop mapRow(ResultSet rs, int arg1) throws SQLException {
				Shop shop = new Shop();
				shop.setShopId(rs.getInt("shop_id"));
				shop.setShopOwner(rs.getString("shop_owner"));
				shop.setShopOwnerIdcrad(rs.getString("shop_owner_idcard"));
				shop.setShopPhone(rs.getString("shop_phone"));
				shop.setShopQQ(rs.getString("shop_qq"));
				shop.setShopName(rs.getString("shop_name"));
				shop.setShopAdr(rs.getString("shop_adr"));
				shop.setShopWorkTime(rs.getString("shop_work_time"));
				shop.setShopSaleType(rs.getString("shop_sale_type"));
				shop.setShopBusinessLiscence(rs.getString("shop_business_licence"));
				shop.setShopIcon(rs.getString("shop_icon"));
				shop.setShopAuthentyStatus(rs.getInt("shop_authenty_status"));
				shop.setShopOwnerId(rs.getInt("shop_owner_id"));
				shop.setLatitude(rs.getDouble("latitude"));
				shop.setLongtitude(rs.getDouble("longtitude"));
				shop.setType(rs.getInt("type"));
				return shop;
			}
			
		}, new Object[]{type,latitude-1,latitude+1,longtitude-1,longtitude+1,longtitude,latitude});
		
		return shops;
	}
	
	public void insertShopUndoMsg(String shopsId,int userId,String data,String uuid,String name,String adr){
		String sqlStr = "insert into shop_undo_msg(shop_ids,user_id,msg_data,msg_type,msg_time,uuid,user_name,user_address) values(?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(sqlStr, new Object[] {shopsId,userId,data,1,System.currentTimeMillis(),uuid,name,adr});
	}
	
	//根据消息的uuid删除
	public void deleteShopUndoMsg(String uuid){
		String sqlStr = "delete from shop_undo_msg where uuid=?";
		jdbcTemplate.update(sqlStr, new Object[]{uuid});
	}
	
	//根据uuid查询
	public ShopUndoMsg findUndoMsg(String uuid){
		String sqlStr = "select * from shop_undo_msg where uuid=?";
		final ShopUndoMsg msg = new ShopUndoMsg();
		jdbcTemplate.query(sqlStr, new Object[]{uuid},new RowCallbackHandler() {
			
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				msg.setMsgId(rs.getInt("msg_id"));
				msg.setMsgData(rs.getString("msg_data"));
				msg.setMsgTime(rs.getLong("msg_time"));
				msg.setMsgType(rs.getInt("msg_type"));
				msg.setUserId(rs.getInt("user_id"));
				msg.setUuid(rs.getString("uuid"));
				msg.setUserName(rs.getString("user_name"));
				msg.setUserAddress(rs.getString("user_address"));
			}
		});
		
		return msg;
	}
	
	public List<ShopUndoMsg> findUndoMsgs(String shopId){
		String sqlStr = "select * from shop_undo_msg where shop_ids like '%&"+shopId+"&%'";
		List<ShopUndoMsg>  list = jdbcTemplate.query(sqlStr,new RowMapper<ShopUndoMsg>(){

			@Override
			public ShopUndoMsg mapRow(ResultSet rs, int arg1)throws SQLException {
				ShopUndoMsg msg = new ShopUndoMsg();
				msg.setMsgId(rs.getInt("msg_id"));
				msg.setMsgData(rs.getString("msg_data"));
				msg.setMsgTime(rs.getLong("msg_time"));
				msg.setMsgType(rs.getInt("msg_type"));
				msg.setUserId(rs.getInt("user_id"));
				msg.setUuid(rs.getString("uuid"));
				msg.setUserName(rs.getString("user_name"));
				msg.setUserAddress(rs.getString("user_address"));
				return msg;
			}
			
		});//new Object[]{"&"+shopId+"&"}
		
		return list;
	}
}
