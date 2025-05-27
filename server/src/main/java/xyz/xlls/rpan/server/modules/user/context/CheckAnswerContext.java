package xyz.xlls.rpan.server.modules.user.context;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 校验密保答案Context对象
 */
@Data
public class CheckAnswerContext implements Serializable {

    private static final long serialVersionUID = -8656511741593485091L;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 密保问题
     */
    private String question;
    /**
     * 密保答案
     */
    private String answer;
}
