package xyz.xlls.rpan.storage.engine.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import xyz.xlls.rpan.cache.core.constants.CacheConstants;
import xyz.xlls.rpan.core.exception.RPanFrameworkException;

import java.util.Objects;

/**
 * 顶级存储引擎的公用父类
 */
public abstract class AbstractStorageEngine implements StorageEngine{
    @Autowired
    private CacheManager cacheManager;

    /**
     * 功用的获取缓存的方法
     * @return
     */
    protected Cache getCache() {
        if(Objects.isNull(cacheManager)){
            throw new RPanFrameworkException("the cache manager is empty!");
        }
        return cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
    }
}
