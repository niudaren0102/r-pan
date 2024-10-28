package xyz.xlls.rpan.cache.caffeine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Caffeine Cache自定义配置类属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "xyz.xlls.rpan.cache.caffeine")
public class CaffeineCacheProperties {
    /**
     * 缓存初始容量
     * xyz.xlls.rpan.cache.caffeine.init-cache-capacity
     */
    private Integer initialCacheCapacity = 256;
    /**
     * 缓存做大容量，超过之后会按照recently or very often （最近最少） 策略进行缓存剔除
     * xyz.xlls.rpan.cache.caffeine.max-cache-capacity
     */
    private Long maxCacheCapacity=10000L;
    /**
     * 是否允许空值null作为缓存的value
     * xyz.xlls.rpan.cache.caffeine.allow-null-value
     */
    private Boolean allowNullValues = Boolean.TRUE;
}
