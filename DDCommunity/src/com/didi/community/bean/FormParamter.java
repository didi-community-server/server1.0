package com.didi.community.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class FormParamter implements Serializable{

	private static final long serialVersionUID = 2185719726417704485L;

	private String type;
	private Map<String , String> paramters ;
	
	public FormParamter(){
		paramters = new HashMap<String, String>();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, String> getParamters() {
		return paramters;
	}

	public void setParamters(Map<String, String> paramters) {
		this.paramters = paramters;
	};
	
	public void putParamter(String key,String value){
		paramters.put(key, value);
	}
}
