package backend.database;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache<T> {
	private ConcurrentHashMap<String, T> _cache; 
	
	public Cache() {
		_cache = new ConcurrentHashMap<>(); 
	}
	
	public boolean contains(String key) {
		return _cache.containsKey(key); 
	}
	
	public T insert(String key, T value) {
		return _cache.put(key, value); 
	}
	
	public void insertAll(Map<? extends String, ? extends T> map) {
		_cache.putAll(map);
	}
	
	public T get(String key) {
		return _cache.get(key); 
	}
}
