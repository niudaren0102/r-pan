package xyz.xlls.rpan.cache.redis.test.instance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.cache.core.constants.CacheConstants;

/**
 * Cache注解测试实体
 */
@Component
@Slf4j
public class CacheAnnotationTester {
    /**
     * 测试自适应缓存注解
     * @param name
     * @return
     */
    @Cacheable(cacheNames = CacheConstants.R_PAN_CACHE_NAME,key = "#name",sync = true)
    public String testCacheable(String name){
        log.info("call xyz.xlls.rpan.cache.caffeine.test.config.instance.CacheAnnotationTester.testCacheable,param is {}",name);
        return new StringBuffer("hello ").append(name).toString();
    }
}
