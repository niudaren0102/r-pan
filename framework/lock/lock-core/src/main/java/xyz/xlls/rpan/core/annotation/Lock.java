package xyz.xlls.rpan.core.annotation;

import xyz.xlls.rpan.core.key.KeyGenerator;
import xyz.xlls.rpan.core.key.StandardKeyGenerator;

import java.lang.annotation.*;

/**
 * 自定义锁的注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Lock {
    /**
     * 锁的名称
     * @return
     */
    String name() default "";

    /**
     * 锁的过期时长
     * @return
     */
    long expireSeconds() default 60L;

    /**
     * 自定义锁的key支持el表达
     * @return
     */
    String[] keys() default {};

    /**
     * 指定锁key的生成器
     * @return
     */
    Class<? extends KeyGenerator> keyGenerator() default StandardKeyGenerator.class;
}
