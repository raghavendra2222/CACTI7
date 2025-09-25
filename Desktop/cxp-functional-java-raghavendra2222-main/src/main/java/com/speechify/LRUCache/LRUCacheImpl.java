package com.speechify.LRUCache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCacheImpl<T> implements LRUCache<T> {
    private final Map<String , T> cache;
    
    public LRUCacheImpl(int capacity){
        this.cache = new LinkedHashMap<String , T>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String , T> eldest) {
                return size() > capacity;
            }
        };
    }
    @Override
    public synchronized T get(String key) {
        return cache.getOrDefault(key, null);
    }

    @Override
    public synchronized void set(String key, T value) {
        cache.put(key, value);
    }
}