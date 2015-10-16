package com.didi.community.socket;

import java.util.HashMap;
import java.util.Map;

public class IntMap<V> {

	private Map<Integer,V> map = new HashMap<Integer,V>();
	
	public IntMap(){
		
	}
	
	public void put(int id,V v){
		map.put(id, v);
	}
	
	public V remove(int id){
		return map.remove(id);
	}
}
