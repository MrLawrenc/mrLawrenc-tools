package com.github.mrLawrenc.cache.serializable.impl;


import com.github.mrLawrenc.cache.serializable.SerializableCache;
import com.github.mrLawrenc.thread.SerializableRunnable;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author  hz20035009-逍遥
 * date  2020/5/26 13:49
 * no sql缓存
 */
public class RedisSerializableCache implements SerializableCache {
    private RedisTemplate<String,String> redisTemplate;

    public RedisSerializableCache(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean save(String key, SerializableRunnable runnable) {
        return false;
    }

    @Override
    public CompletableFuture<?> asyncSave(String key, SerializableRunnable runnable) {
        return null;
    }

    @Override
    public List<SerializableRunnable> list(String key) {
        return null;
    }

    @Override
    public List<SerializableRunnable> asyncList(String key) {
        return null;
    }
}