package org.semantics.apigateway.service;


import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Arrays;

@Service
public class CacheService {

    private final CacheManager cacheManager;

    @Autowired
    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public boolean exists(String key) {
        Cache cache = cacheManager.getCache("defaultCache");  // You can customize cache name here
        return cache != null && cache.get(key) != null;
    }

    public void write(String key, Object data) {
        Cache cache = cacheManager.getCache("defaultCache");
        if (cache != null) {
            cache.put(key, data);  // Put data in the cache
        }
    }

    public Object read(String key) {
        Cache cache = cacheManager.getCache("defaultCache");
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            return valueWrapper != null ? valueWrapper.get() : null;  // Return null if the key doesn't exist
        }
        return null;
    }
}
