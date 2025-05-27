package xyz.xlls.rpan.server.modules.user.context;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户在线修改密码Context对象
 */
@Data
public class ChangePasswordContext implements Serializable {
    private static final long serialVersionUID = 5879044704878789748L;
    /**
     * 当前登录的用户id
     */
    private Long userId;
    /**
     * 旧密码
     */
    private String oldPassword;
    /**
     * 新密码
     */
    private String newPassword;
    /**
     * 当前登录用户的实体信息
     */
    private RPanUser entity;
}
