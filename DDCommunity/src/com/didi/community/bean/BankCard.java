package com.didi.community.bean;

import java.io.Serializable;

public class BankCard implements Serializable{

	private static final long serialVersionUID = 1L;

	private int cardId;
	private String owner;
	private String card;
	private String adr;
	private int shopId;
	
	public BankCard(){
		
	}

	public int getCardId() {
		return cardId;
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getCard() {
		return card;
	}

	public void setCard(String card) {
		this.card = card;
	}

	public String getAdr() {
		return adr;
	}

	public void setAdr(String adr) {
		this.adr = adr;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}
	
	
}
