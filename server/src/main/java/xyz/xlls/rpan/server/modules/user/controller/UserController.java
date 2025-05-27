package xyz.xlls.rpan.server.modules.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.xlls.rpan.core.response.R;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.annotation.LoginIgnore;
import xyz.xlls.rpan.server.common.utils.UserIdUtil;
import xyz.xlls.rpan.server.modules.user.context.*;
import xyz.xlls.rpan.server.modules.user.converter.UserConverter;
import xyz.xlls.rpan.server.modules.user.po.*;
import xyz.xlls.rpan.server.modules.user.service.IUserService;

/**
 * 该类是用户模块的控制器实体
 */
@RestController
@RequestMapping("user")
@Api(tags = "用户模块")
public class UserController {
    @Autowired
    private IUserService userService;
    @Autowired
    private UserConverter  userConverter;
    @ApiOperation(
            value = "用户注册接口",
            notes = "该模块提供了用户注册的功能，实现了幂等性注册的逻辑，可以放心多并发调用",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("register")
    public R register(@Validated @RequestBody UserRegisterPO userRegisterPO){
        UserRegisterContext userRegisterContext = userConverter.registerPO2UserRegisterContext(userRegisterPO);
        Long userId= userService.register(userRegisterContext);
        return R.data(IdUtil.encrypt(userId));
    }

    @ApiOperation(
            value = "用户登陆接口",
            notes = "该模块提供了用户登陆的功能，成功登陆之后，会返回有时效性的accessToken供后续服务使用",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("login")
    @LoginIgnore
    public R login(@Validated @RequestBody UserLoginPO userLoginPO){
        UserLoginContext userLoginContext = userConverter.userLoginPO2UserLoginContext(userLoginPO);
        String accessToken=userService.login(userLoginContext);
        return R.data(accessToken);
    }

    @ApiOperation(
            value = "用户登出接口",
            notes = "该模块提供了用户登出的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("exit")
    public R exit(){
        userService.exit(UserIdUtil.get());
        return R.success();
    }
    @ApiOperation(
            value = "用户忘记密码-校验用户名",
            notes = "该模块提供了用户忘记密码-校验用户名的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("username/check")
    public  R checkUsername(@Validated @RequestBody CheckUsernamePO checkUsernamePO){
        CheckUsernameContext checkUsernameContext=userConverter.checkUsernamePO2CheckUsernameContext(checkUsernamePO);
        String question=userService.checkUsername(checkUsernameContext);
        return R.data(question);
    }
    @ApiOperation(
            value = "用户忘记密码-校验密保答案",
            notes = "该模块提供了用户忘记密码-校验密保答案的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("answer/check")
    public  R checkAnswer(@Validated @RequestBody CheckAnswerPO checkAnswerPO){
        CheckAnswerContext checkAnswerContext=userConverter.checkAnswerPO2CheckAnswerContext(checkAnswerPO);
        String token=userService.checkAnswer(checkAnswerContext);
        return R.data(token);
    }
    @ApiOperation(
            value = "用户忘记密码-重置密码",
            notes = "该模块提供了用户忘记密码-重置密码的功能",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("password/reset")
    public  R resetPassword(@Validated @RequestBody ResetPasswordPO resetPasswordPO){
        ResetPasswordContext resetPasswordContext=userConverter.resetPasswordPO2ResetPasswordContext(resetPasswordPO);
        userService.resetPassword(resetPasswordContext);
        return R.success();
    }
}
