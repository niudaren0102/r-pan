package xyz.xlls.rpan.server.modules.user.service;

import xyz.xlls.rpan.server.modules.user.context.*;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【r_pan_user(用户信息表)】的数据库操作Service
* @createDate 2024-10-22 14:59:10
*/
public interface IUserService extends IService<RPanUser> {
    /**
     * 用户注册业务
     * @param userRegisterContext
     * @return
     */
    Long register(UserRegisterContext userRegisterContext);

    /**
     * 用户登陆业务
     * @param userLoginContext
     * @return
     */
    String login(UserLoginContext userLoginContext);

    /**
     * 用户退出登录
     * @param userId
     */
    void exit(Long userId);

    /**
     * 用户名忘记密码-校验用户名
     * @param checkUsernameContext
     * @return
     */
    String checkUsername(CheckUsernameContext checkUsernameContext);

    /**
     * 用户名忘记密码-校验密保答案
     * @param checkAnswerContext
     * @return
     */
    String checkAnswer(CheckAnswerContext checkAnswerContext);

    /**
     * 重置用户密码
     * @param resetPasswordContext
     */
    void resetPassword(ResetPasswordContext resetPasswordContext);
}
