package xyz.xlls.rpan.server.modules.user;

import cn.hutool.core.lang.Assert;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import xyz.xlls.rpan.core.exception.RPanBusinessException;
import xyz.xlls.rpan.server.RPanServerLauncher;
import xyz.xlls.rpan.server.modules.user.context.UserLoginContext;
import xyz.xlls.rpan.server.modules.user.context.UserRegisterContext;
import xyz.xlls.rpan.server.modules.user.service.IUserService;

/**
 * 用户模块单元测试类
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RPanServerLauncher.class)
@Transactional
public class UserTest {
    @Autowired
    private IUserService userService;

    /**
     * 测试成功注册用户信息
     */
    @Test
    public void testRegisterUser() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
    }

    /**
     * 测试重复用户名称注册幂等
     */
    @Test(expected = RPanBusinessException.class)
    public void testRegisterDuplicateUsername() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        register = userService.register(context);
    }

    /**
     * 测试登录成功
     */
    @Test(expected = RPanBusinessException.class)
    public void testLoginSuccess() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        UserLoginContext userLoginContext=new UserLoginContext();
        String accessToken = userService.login(userLoginContext);
        Assert.isTrue(StringUtils.isNotBlank(accessToken));
    }

    /**
     * 测试登录失败：用户名不正确
     */
    @Test(expected = RPanBusinessException.class)
    public void testWrongUsername() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        UserLoginContext userLoginContext=new UserLoginContext();
        userLoginContext.setUsername(userLoginContext.getUsername()+"_change");
        String accessToken = userService.login(userLoginContext);
    }

    /**
     * 测试登录失败：密码不正确
     */
    @Test(expected = RPanBusinessException.class)
    public void testWrongPassword() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        UserLoginContext userLoginContext=new UserLoginContext();
        userLoginContext.setPassword(userLoginContext.getPassword()+"_change");
        String accessToken = userService.login(userLoginContext);
    }
    /**
     * 构建注册用户上下文信息
     * @return
     */
    private UserRegisterContext createUserRegisterContext() {
        UserRegisterContext context=new UserRegisterContext();
        context.setUsername("admin");
        context.setPassword("123456");
        context.setQuestion("test");
        context.setAnswer("test");
        return context;

    }

    /**
     * 构建登录用户上下文信息
     * @return
     */
    private UserLoginContext createUserLoginContext() {
        UserLoginContext context=new UserLoginContext();
        context.setUsername("admin");
        context.setPassword("123456");
        return context;

    }
}
