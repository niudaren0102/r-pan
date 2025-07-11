package xyz.xlls.rpan.server.common.annotation;

import java.lang.annotation.*;

/**
 * 该接口主要影响需要分享码校验的接口
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface NeedShareCode {
}
