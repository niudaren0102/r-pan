package xyz.xlls.rpan.server.modules.user.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 用户登陆参数实体对象
 */
@Data
@ApiModel(value = "用户登陆参数")
public class UserLoginPO implements Serializable {

    private static final long serialVersionUID = -3815261251028358590L;
    @ApiModelProperty(value = "用户名",required = true)
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[0-9A-Za-z]{6,16}$",message = "请输入6-16位只包含数字和字母的用户名")
    private String username;
    @ApiModelProperty(value = "密码",required = true)
    @NotBlank(message = "密码不能为空")
    @Length(min = 8,max = 16,message = "请输入8-16位密码")
    private String password;
}
