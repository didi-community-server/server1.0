package com.didi.community.bean;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class ResponseJson<T> implements Serializable {

	private static final long serialVersionUID = -4784834011021322437L;

	@Expose
	private int status;
	
	@Expose
	private String info;
	
	@Expose
	private  T data;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
	public static  ResponseJson fromJson(String json, Class clazz) {
        Gson gson = new Gson();
        Type objectType = type(ResponseJson.class, clazz);
        return gson.fromJson(json, objectType);
    }
	
	/**
	 * 只导出注解
	 * @param json
	 * @param clazz
	 * @param expose
	 * @return
	 */
	public static ResponseJson fromJson(String json, Class clazz,boolean expose){
		 Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	        Type objectType = type(ResponseJson.class, clazz);
	        return gson.fromJson(json, objectType);
	}

    public String toJson(Class<T> clazz) {
    	Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Type objectType = type(ResponseJson.class, clazz);
        return gson.toJson(this, objectType);
    }

    private static ParameterizedType type(final Class raw, final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return raw;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }
        };
    }
	
}
