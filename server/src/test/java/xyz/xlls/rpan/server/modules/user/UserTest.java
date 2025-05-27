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
import xyz.xlls.rpan.core.utils.JwtUtil;
import xyz.xlls.rpan.server.RPanServerLauncher;
import xyz.xlls.rpan.server.modules.user.constants.UserConstants;
import xyz.xlls.rpan.server.modules.user.context.*;
import xyz.xlls.rpan.server.modules.user.service.IUserService;
import xyz.xlls.rpan.server.modules.user.vo.UserInfoVO;

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
     * 测试登录失败
     */
    @Test(expected = RPanBusinessException.class)
    public void testExitSuccess() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        UserLoginContext userLoginContext=new UserLoginContext();
        String accessToken = userService.login(userLoginContext);
        Assert.isTrue(StringUtils.isNotBlank(accessToken));
        Long userId=(Long) JwtUtil.analyzeToken(accessToken, UserConstants.LOGIN_USER_ID);
        userService.exit(userId);
    }

    /**
     * 校验用户名称通过
     */
    @Test
    public void checkUsernameSuccess() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        CheckUsernameContext usernameContext=new CheckUsernameContext();
        usernameContext.setUsername("admin");
        String question = userService.checkUsername(usernameContext);
        Assert.isTrue(StringUtils.isNotBlank(question));
    }

    /**
     * 校验用户名称失败-没有找到该用户
     */
    @Test(expected = RPanBusinessException.class)
    public void checkUsernameNotExist() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        CheckUsernameContext usernameContext=new CheckUsernameContext();
        usernameContext.setUsername("admin_changed");
        String question = userService.checkUsername(usernameContext);
        Assert.isTrue(StringUtils.isBlank(question));
    }

    /**
     * 校验用户名称通过
     */
    @Test
    public void checkAnswerSuccess() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        CheckAnswerContext checkAnswerContext=new CheckAnswerContext();
        checkAnswerContext.setUsername("admin");
        checkAnswerContext.setQuestion("test");
        checkAnswerContext.setAnswer("test");
        String token=userService.checkAnswer(checkAnswerContext);
        Assert.isTrue(StringUtils.isNotBlank(token));
    }

    /**
     * 校验用户名称失败-没有找到该用户
     */
    @Test(expected = RPanBusinessException.class)
    public void checkAnswerFail() {
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        CheckAnswerContext checkAnswerContext=new CheckAnswerContext();
        checkAnswerContext.setUsername("admin");
        checkAnswerContext.setQuestion("test");
        checkAnswerContext.setAnswer("test_changed");
        String token=userService.checkAnswer(checkAnswerContext);
        Assert.isTrue(StringUtils.isNotBlank(token));
    }

    /**
     * 正常重置用户密码
     */
    @Test
    public void resetPasswordSuccess(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        CheckAnswerContext checkAnswerContext=new CheckAnswerContext();
        checkAnswerContext.setUsername("admin");
        checkAnswerContext.setQuestion("test");
        checkAnswerContext.setAnswer("test");
        String token=userService.checkAnswer(checkAnswerContext);
        ResetPasswordContext resetPasswordContext=new ResetPasswordContext();
        resetPasswordContext.setUsername("admin");
        resetPasswordContext.setPassword("123456_changed");
        resetPasswordContext.setToken(token);
        userService.resetPassword(resetPasswordContext);
    }

    /**
     * 用户重置密码失败-token异常
     */
    @Test(expected = RPanBusinessException.class)
    public void resetPasswordTokenError(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        CheckAnswerContext checkAnswerContext=new CheckAnswerContext();
        checkAnswerContext.setUsername("admin");
        checkAnswerContext.setQuestion("test");
        checkAnswerContext.setAnswer("test");
        String token=userService.checkAnswer(checkAnswerContext);
        ResetPasswordContext resetPasswordContext=new ResetPasswordContext();
        resetPasswordContext.setUsername("admin");
        resetPasswordContext.setPassword("123456_changed");
        resetPasswordContext.setToken(token+"_changed");
        userService.resetPassword(resetPasswordContext);
    }
    /**
     * 正常在线修改密码
     */
    @Test
    public void changePasswordSuccess(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        ChangePasswordContext changePasswordContext=new ChangePasswordContext();
        changePasswordContext.setUserId(register);
        changePasswordContext.setOldPassword("123456");
        changePasswordContext.setNewPassword("123456_changed");
        userService.changePassword(changePasswordContext);
    }
    @Test
    public void testQueryUserInfo(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        UserInfoVO info = userService.info(register);
        Assert.notNull(info);
    }

    /**
     * 修改密码失败-旧密码错误
     */
    @Test(expected = RPanBusinessException.class)
    public void changePasswordFailByWrongOldPassword(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = userService.register(context);
        Assert.isTrue(register >0L);
        ChangePasswordContext changePasswordContext=new ChangePasswordContext();
        changePasswordContext.setUserId(register);
        changePasswordContext.setOldPassword("123456_changed");
        changePasswordContext.setNewPassword("123456_changed");
        userService.changePassword(changePasswordContext);
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
