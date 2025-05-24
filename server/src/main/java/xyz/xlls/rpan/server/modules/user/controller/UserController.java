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
import xyz.xlls.rpan.server.modules.user.context.UserLoginContext;
import xyz.xlls.rpan.server.modules.user.context.UserRegisterContext;
import xyz.xlls.rpan.server.modules.user.converter.UserConverter;
import xyz.xlls.rpan.server.modules.user.po.UserLoginPO;
import xyz.xlls.rpan.server.modules.user.po.UserRegisterPO;
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
}
