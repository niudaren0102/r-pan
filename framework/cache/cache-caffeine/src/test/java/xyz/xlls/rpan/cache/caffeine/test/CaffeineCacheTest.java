package xyz.xlls.rpan.cache.caffeine.test;

import cn.hutool.core.lang.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xyz.xlls.rpan.cache.caffeine.test.config.CaffeineCacheConfig;
import xyz.xlls.rpan.cache.caffeine.test.instance.CacheAnnotationTester;
import xyz.xlls.rpan.cache.core.constants.CacheConstants;

/**
 * Caffeine缓存单元测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaffeineCacheConfig.class)
public class CaffeineCacheTest {
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private CacheAnnotationTester cacheAnnotationTester;

    /**
     * 简单测试CacheManager的功能以及获取的Cache对象的功能
     */
    @Test
    public void caffeineCacheManagerTest() {
        Cache cache=cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
        Assert.notNull(cache);
        cache.put("name","value");
        String value= cache.get("name",String.class);
        Assert.isTrue("value".equals(value));
    }
    @Test
    public void caffeineCacheAnnotationTest() {
        for(int i=0;i<2;i++){
            cacheAnnotationTester.testCacheable("imooc");
        }
    }
}
