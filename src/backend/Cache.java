package backend;

import java.util.HashMap;

public class Cache<T> {
	private HashMap<String, T> _cache; 
	
	public Cache() {
		_cache = new HashMap<>(); 
	}
	
	public boolean contains(String key) {
		return _cache.containsKey(key); 
	}
	
	public T insert(String key, T value) {
		return _cache.put(key, value); 
	}
	
	public T get(String key) {
		return _cache.get(key); 
	}
}
