package xyz.xlls.rpan.server.modules.user.context;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 重置用户密码Context对象
 */
@Data
public class ResetPasswordContext implements Serializable {

    private static final long serialVersionUID = 4108192387990768946L;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 用户新密码
     */
    private String password;
    /**
     * 重置密码的token信息
     */
    private String token;
}
