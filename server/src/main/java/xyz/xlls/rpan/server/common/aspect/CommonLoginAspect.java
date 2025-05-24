package xyz.xlls.rpan.server.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;
import xyz.xlls.rpan.cache.core.constants.CacheConstants;
import xyz.xlls.rpan.core.response.R;
import xyz.xlls.rpan.core.response.ResponseCode;
import xyz.xlls.rpan.core.utils.JwtUtil;
import xyz.xlls.rpan.server.common.annotation.LoginIgnore;
import xyz.xlls.rpan.server.common.utils.UserIdUtil;
import xyz.xlls.rpan.server.modules.user.constants.UserConstants;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 统一的登录拦截校验切面逻辑实现类
 */
@Component
@Aspect
@Slf4j
public class CommonLoginAspect {
    /**
     * 登录认证参数名称
     */
    private static final String LOGIN_AUTH_PARAM_NAME="authorization";
    /**
     * 请求头认证key
     */
    private static final String LOGIN_AUTH_REQUEST_HEADER_NAME="Authorization";
    /**
     * 切点表达式
     */
    private final static String POINT_CUT="execution(* xyz.xlls.rpan.server.modules.*.controller..*(..))";
    @Autowired
    private CacheManager cacheManager;

    /**
     * 切点模板方法
     */
    @Pointcut(value = POINT_CUT)
    public void loginAuth() {}

    /**
     * 切点的环绕增强逻辑
     * 1、需要判断需不需要校验登录信息
     * 2、校验登录信息：
     *  a、获取token从请求头获取参数
     *  b、从缓存中获取token，进行比对
     *  c、解析token
     *  d、解析的UserId存入线程上下文，供下游使用
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("loginAuth()")
    public Object loginAuthAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if(checkNeedCheckLoginInfo(joinPoint)){
            ServletRequestAttributes servletRequestAttributes=(ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String requestURI = request.getRequestURI();
            log.info("成功拦截到请求，URI为：{}",requestURI);
            if(!checkAndSaveUserId(request)){
                log.warn("成功拦截到请求，URI为：{}。检测到用户未登录，将跳转至登录页面",requestURI);
                return R.fail(ResponseCode.NEED_LOGIN);
            }
            log.info("成功拦截到请求，URI为：{}，请求通过",requestURI);
        }
        return joinPoint.proceed();
    }

    /**
     * 校验token并提取userId
     * @param request
     * @return
     */
    private boolean checkAndSaveUserId(HttpServletRequest request) {
        String accessToken = request.getHeader(LOGIN_AUTH_REQUEST_HEADER_NAME);
        if(StringUtils.isBlank(accessToken)){
            accessToken = request.getParameter(LOGIN_AUTH_PARAM_NAME);
        }
        if(StringUtils.isBlank(accessToken)){
            return false;
        }
        Object userId = JwtUtil.analyzeToken(accessToken, UserConstants.LOGIN_USER_ID);
        if(Objects.isNull(userId)){
            return false;
        }
        Cache cache = cacheManager.getCache(CacheConstants.R_PAN_CACHE_NAME);
        Object redisAccessToken= cache.get(UserConstants.USER_LOGIN_PREFIX+userId);
        if(Objects.isNull(redisAccessToken)){
            return false;
        }
        if(Objects.equals(accessToken, redisAccessToken)){
            saveUserId(userId);
            return true;
        }
        return false;
    }

    private void saveUserId(Object userId) {
        UserIdUtil.set(Long.valueOf(String.valueOf(userId)));
    }

    /**
     *
     * @param joinPoint
     * @return true需要检验登录信息，false不需要
     */
    private boolean checkNeedCheckLoginInfo(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        return !method.isAnnotationPresent(LoginIgnore.class);
    }
}
