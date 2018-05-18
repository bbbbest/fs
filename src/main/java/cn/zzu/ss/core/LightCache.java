package cn.zzu.ss.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public final class LightCache<K, V> {
    private static final int MAX_SIZE = 4096;
    private Cache<K, V> cache;

    public LightCache() {
        cache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE).build();
    }

    public LightCache(int capacity) {
        cache = CacheBuilder.newBuilder().maximumSize(capacity).build();
    }

    public V getIfPresent(K key) {
        return cache.getIfPresent(key);
    }

    public V getIfPresent(K key, Callable<V> callable) throws ExecutionException {
        return cache.get(key, callable);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public void clear() {
        cache.cleanUp();
    }

    public long size() {
        return cache.size();
    }
}
