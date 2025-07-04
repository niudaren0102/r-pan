package xyz.xlls.rpan.cache.caffeine.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import xyz.xlls.rpan.cache.core.constants.CacheConstants;

@SpringBootConfiguration
@EnableCaching
@Slf4j
public class CaffeineCacheConfig {
    @Autowired
    private CaffeineCacheProperties properties;
    @Bean
    public CacheManager  caffeineCacheManager(){
        CaffeineCacheManager cacheManager=new CaffeineCacheManager(CacheConstants.R_PAN_CACHE_NAME );
        cacheManager.setAllowNullValues(properties.getAllowNullValues());
        Caffeine<Object,Object> caffeineBuilder=Caffeine.newBuilder()
                .initialCapacity(properties.getInitialCacheCapacity())
                .maximumSize(properties.getMaxCacheCapacity());
        cacheManager.setCaffeine(caffeineBuilder);
        log.info("the caffeine cache manager is loaded successfully!");
        return cacheManager;
    }

}
