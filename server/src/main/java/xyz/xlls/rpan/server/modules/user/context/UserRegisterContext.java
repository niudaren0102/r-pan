package xyz.xlls.rpan.server.modules.user.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;

import java.io.Serializable;

/**
 * 用户注册业务的上下文实体对象
 */
@Data
public class UserRegisterContext implements Serializable {

    private static final long serialVersionUID = 7299854444139599876L;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 密保问题
     */
    private String question;
    /**
     * 密保答案
     */
    private String answer;
    /**
     * 用户实体对象
     */
    private RPanUser entity;
}
