package xyz.xlls.rpan.server.modules.user.service;

import xyz.xlls.rpan.server.modules.user.context.UserRegisterContext;
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
}
