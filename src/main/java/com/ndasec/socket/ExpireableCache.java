package com.ndasec.socket;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 可过期的缓存对象
 */
public class ExpireableCache {

    /**
     * 缓存对象
     */
    private final Map<String, CacheObj> cacheObjMap;

    public ExpireableCache(){
        this.cacheObjMap  = new ConcurrentHashMap<>();
    }

    public ExpireableCache(int initSize) {
        this.cacheObjMap  = new ConcurrentHashMap<>(initSize);
    }


    /**
     * 设置缓存
     */
    public void setCache(String cacheKey, Object cacheValue, long cacheTime) {
        Long ttlTime = null;
        if (cacheTime <= 0L) {
            if (cacheTime == -1L) {
                ttlTime = -1L;
            } else {
                return;
            }
        }
        if (ttlTime == null) {
            ttlTime = System.currentTimeMillis() + cacheTime;
        }
        cacheObjMap.put(cacheKey, new CacheObj(cacheValue, ttlTime));
    }

    /**
     * 设置缓存
     */
    public void setCache(String cacheKey, Object cacheValue) {
        setCache(cacheKey, cacheValue, -1L);
    }

    /**
     * 获取缓存
     */
    public Object getCache(String cacheKey) {
        if (checkCache(cacheKey)) {
            return cacheObjMap.get(cacheKey).getCacheValue();
        }
        return null;
    }

    public boolean isExist(String cacheKey) {
        return checkCache(cacheKey);
    }

    /**
     * 删除所有缓存
     */
    public void clear() {
        cacheObjMap.clear();
    }

    /**
     * 删除某个缓存
     */
    public Object deleteCache(String cacheKey) {
        CacheObj remove = cacheObjMap.remove(cacheKey);
        if (remove != null) {
            return remove.getCacheValue();
        }
        return null;
    }

    /**
     * 判断缓存在不在,过没过期
     */
    public boolean checkCache(String cacheKey) {
        CacheObj cacheObj = cacheObjMap.get(cacheKey);
        if (cacheObj == null) {
            return false;
        }
        if (cacheObj.getTtlTime() == -1L) {
            return true;
        }
        if (cacheObj.getTtlTime() < System.currentTimeMillis()) {
            deleteCache(cacheKey);
            return false;
        }
        return true;
    }

    /**
     * 删除过期的缓存
     */
    public List<String> deleteTimeOut() {
        List<String> deleteKeyList = new LinkedList<>();
        for (Map.Entry<String, CacheObj> entry : cacheObjMap.entrySet()) {
            if (entry.getValue().getTtlTime() < System.currentTimeMillis() && entry.getValue().getTtlTime() != -1L) {
                deleteKeyList.add(entry.getKey());
            }
        }
        for (String deleteKey : deleteKeyList) {
            deleteCache(deleteKey);
        }
        return deleteKeyList;

    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        ExpireableCache expireableCache = new ExpireableCache();
        for (int i = 0; i < 10000; i++) {
            expireableCache.setCache("my_cache_key_" + i, i, 600);
        }

        for (int i = 0; i < 10000; i++) {
            expireableCache.getCache("my_cache_key_" + i);
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(expireableCache);
    }
}

class CacheObj {
    /**
     * 缓存对象
     */
    private Object CacheValue;
    /**
     * 缓存过期时间
     */
    private Long ttlTime;

    CacheObj(Object cacheValue, Long ttlTime) {
        CacheValue = cacheValue;
        this.ttlTime = ttlTime;
    }

    Object getCacheValue() {
        return CacheValue;
    }

    Long getTtlTime() {
        return ttlTime;
    }

    @Override
    public String toString() {
        return "CacheObj{" + "CacheValue=" + CacheValue + ", ttlTime=" + ttlTime + '}';
    }
}