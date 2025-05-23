package xyz.xlls.rpan.server.modules.user.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;

import java.io.Serializable;

/**
 * 用户登陆业务的上下文实体对象
 */
@Data
public class UserLoginContext implements Serializable {

    private static final long serialVersionUID = -4865173244077659258L;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 用户实体对象
     */
    private RPanUser entity;
    /**
     * 登录成功后的凭证信息
     */
    private String accessToken;
}
