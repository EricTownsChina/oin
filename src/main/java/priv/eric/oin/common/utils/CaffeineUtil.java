package priv.eric.oin.common.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * Desc: caffeine本地缓存
 *
 * @author EricTownsChina@outlook.com
 * create 2022/3/12 23:44
 */
public class CaffeineUtil {

    private static Cache<String, Object> DEFAULT_CACHE = null;

    private CaffeineUtil() {
    }

    public static Cache<String, Object> instance(int max, long timeout, TimeUnit timeUnit) {
        return Caffeine.newBuilder()
                .expireAfterWrite(timeout, timeUnit)
                .maximumSize(max)
                .build();
    }

    /**
     * 默认配置的缓存存
     *
     * @param key  缓存数据key
     * @param data 缓存数据
     */
    public static void put(String key, Object data) {
        if (null == DEFAULT_CACHE) {
            DEFAULT_CACHE = instance(100, 1, TimeUnit.DAYS);
        }
        DEFAULT_CACHE.put(key, data);
    }

    /**
     * 默认配置的缓存取
     *
     * @param key 缓存数据key
     * @return 缓存数据
     */
    public static Object get(String key) {
        if (null == DEFAULT_CACHE) {
            return null;
        }
        return DEFAULT_CACHE.getIfPresent(key);
    }

}
