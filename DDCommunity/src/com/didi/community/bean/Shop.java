package com.didi.community.bean;

import java.io.Serializable;
import java.util.List;

public class Shop implements Serializable{

	private static final long serialVersionUID = -1829279287010934016L;

	private int shopId;
	private String shopOwner;//商家所有者
	private String shopOwnerIdcrad;
	private String shopPhone;
	private String shopQQ;
	private List<BankCard> cards;
	private String shopName;
	private String shopAdr;
	private String shopWorkTime;
	private String shopSaleType;
	private String shopBusinessLiscence;
	private String shopIcon;
	private int shopOwnerId;
	private int shopAuthentyStatus;
	private double latitude;//
    private double longtitude;//
    private int canRobCount;//可抢单数量
    private int type;//商家类型 0:餐饮,1:果蔬,2:百货,3:家政,4:其他
	
	public Shop(){
		setShopIcon("");
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}

	public String getShopOwner() {
		return shopOwner;
	}

	public void setShopOwner(String shopOwner) {
		this.shopOwner = shopOwner;
	}

	public String getShopOwnerIdcrad() {
		return shopOwnerIdcrad;
	}

	public void setShopOwnerIdcrad(String shopOwnerIdcrad) {
		this.shopOwnerIdcrad = shopOwnerIdcrad;
	}

	public String getShopPhone() {
		return shopPhone;
	}

	public void setShopPhone(String shopPhone) {
		this.shopPhone = shopPhone;
	}

	public String getShopQQ() {
		return shopQQ;
	}

	public void setShopQQ(String shopQQ) {
		this.shopQQ = shopQQ;
	}

	
	public List<BankCard> getCards() {
		return cards;
	}

	public void setCards(List<BankCard> cards) {
		this.cards = cards;
	}
	
	public void addCard(BankCard card){
		this.cards.add(card);
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public String getShopAdr() {
		return shopAdr;
	}

	public void setShopAdr(String shopAdr) {
		this.shopAdr = shopAdr;
	}

	public String getShopWorkTime() {
		return shopWorkTime;
	}

	public void setShopWorkTime(String shopWorkTime) {
		this.shopWorkTime = shopWorkTime;
	}

	public String getShopSaleType() {
		return shopSaleType;
	}

	public void setShopSaleType(String shopSaleType) {
		this.shopSaleType = shopSaleType;
	}

	public String getShopBusinessLiscence() {
		return shopBusinessLiscence;
	}

	public void setShopBusinessLiscence(String shopBusinessLiscence) {
		this.shopBusinessLiscence = shopBusinessLiscence;
	}

	public String getShopIcon() {
		return shopIcon;
	}

	public void setShopIcon(String shopIcon) {
		this.shopIcon = shopIcon;
	}

	public int getShopOwnerId() {
		return shopOwnerId;
	}

	public void setShopOwnerId(int shopOwnerId) {
		this.shopOwnerId = shopOwnerId;
	}

	public int getShopAuthentyStatus() {
		return shopAuthentyStatus;
	}

	public void setShopAuthentyStatus(int shopAuthentyStatus) {
		this.shopAuthentyStatus = shopAuthentyStatus;
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

	public int getCanRobCount() {
		return canRobCount;
	}

	public void setCanRobCount(int canRobCount) {
		this.canRobCount = canRobCount;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
}
