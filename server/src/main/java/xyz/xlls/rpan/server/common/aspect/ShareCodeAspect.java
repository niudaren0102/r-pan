package xyz.xlls.rpan.server.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.xlls.rpan.cache.core.constants.CacheConstants;
import xyz.xlls.rpan.core.response.R;
import xyz.xlls.rpan.core.response.ResponseCode;
import xyz.xlls.rpan.core.utils.JwtUtil;
import xyz.xlls.rpan.server.common.annotation.LoginIgnore;
import xyz.xlls.rpan.server.common.utils.ShareIdUtil;
import xyz.xlls.rpan.server.common.utils.UserIdUtil;
import xyz.xlls.rpan.server.modules.share.constants.ShareConstant;
import xyz.xlls.rpan.server.modules.user.constants.UserConstants;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 统一分享码的校验切面实现类
 */
@Component
@Aspect
@Slf4j
public class ShareCodeAspect {
    /**
     * 分享码认证参数名称
     */
    private static final String SHARE_CODE_AUTH_PARAM_NAME = "shareToken";
    /**
     * 请求头认证key
     */
    private static final String SHARE_CODE_AUTH_REQUEST_HEADER_NAME = "Share-Token";
    /**
     * 切点表达式
     */
    private final static String POINT_CUT = "annotation(xyz.xlls.rpan.server.common.annotation.NeedShareCode)";

    /**
     * 切点模板方法
     */
    @Pointcut(value = POINT_CUT)
    public void shareCodeAuth() {

    }

    /**
     * 切点环绕增强逻辑
     * 1、判断需不需要校验分享的token信息
     * 2、校验登录信息
     * a、获取token从请求头获取参数
     * b、解析token
     * c、解析的shareId存入线程上下文，供下游使用
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("shareCodeAuth()")
    public Object shareCodeAuthAround(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String requestURI = request.getRequestURI();
        log.info("成功拦截到请求，URI为：{}", requestURI);
        if (!checkAndSaveShareId(request)) {
            log.warn("成功拦截到请求，URI为：{}。检测到用户的分享码失效，将跳转至分享码校验页面", requestURI);
            return R.fail(ResponseCode.ACCESS_DENIED);
        }
        log.info("成功拦截到请求，URI为：{}，请求通过", requestURI);
        return joinPoint.proceed();
    }

    /**
     * 校验token并提取userId
     *
     * @param request
     * @return
     */
    private boolean checkAndSaveShareId(HttpServletRequest request) {
        String shareToken = request.getHeader(SHARE_CODE_AUTH_REQUEST_HEADER_NAME);
        if (StringUtils.isBlank(shareToken)) {
            shareToken = request.getParameter(SHARE_CODE_AUTH_PARAM_NAME);
        }
        if (StringUtils.isBlank(shareToken)) {
            return false;
        }
        Object shareId = JwtUtil.analyzeToken(shareToken, ShareConstant.SHARE_ID);
        if (Objects.isNull(shareId)) {
            return false;
        }
        saveShareId(shareId);
        return false;
    }

    /**
     * 保存分享ID到线程的上下文中
     * @param userId
     */
    private void saveShareId(Object userId) {
        ShareIdUtil.set(Long.valueOf(String.valueOf(userId)));
    }

}
