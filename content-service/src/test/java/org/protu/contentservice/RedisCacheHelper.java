package org.protu.contentservice;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

@ActiveProfiles("test")
@Component
public class RedisCacheHelper {

  private final CacheManager cacheManager;
  private final RedisTemplate<String, Object> redisTemplate;

  public RedisCacheHelper(CacheManager cacheManager, RedisTemplate<String, Object> redisTemplate) {
    this.cacheManager = cacheManager;
    this.redisTemplate = redisTemplate;
  }

  public void clearAllCaches() {
    cacheManager.getCacheNames().forEach(cacheName -> {
      Cache cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.clear();
      }
    });
  }

  public boolean isCached(String cacheName, Object key) {
    Cache cache = cacheManager.getCache(cacheName);
    return cache != null && cache.get(key) != null;
  }

  public Object getCachedValue(String cacheName, Object key) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache == null) return null;

    Cache.ValueWrapper wrapper = cache.get(key);
    return wrapper != null ? wrapper.get() : null;
  }

  public boolean isCacheEmpty(String cacheName) {
    Set<String> keys = redisTemplate.keys(cacheName + "::*");
    return keys.isEmpty();
  }

  public void printCacheContents(String cacheName) {
    Set<String> allKeys = redisTemplate.keys("*");
    if (!allKeys.isEmpty()) {
      System.out.println("=== All Redis Keys ===");
      allKeys.forEach(key -> {
        Object value = redisTemplate.opsForValue().get(key);
        System.out.println("Key: " + key + " -> Value: " + value);
      });

      System.out.println("=== Keys containing '" + cacheName + "' ===");
      allKeys.stream()
          .filter(key -> key.contains(cacheName))
          .forEach(key -> {
            Object value = redisTemplate.opsForValue().get(key);
            System.out.println("Cache Key: " + key + " -> Value: " + value);
          });
    } else {
      System.out.println("No Redis keys found at all");
    }
  }
}