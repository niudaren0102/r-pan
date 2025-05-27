package xyz.xlls.rpan.server.modules.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.xlls.rpan.web.serializer.IdEncryptSerializer;

import java.io.Serializable;

@ApiModel(value = "用户基本信息返回参数")
@Data
public class UserInfoVO implements Serializable {
    private static final long serialVersionUID = -1557972403041291575L;
    @ApiModelProperty(value = "用户名称")
    private String username;
    @ApiModelProperty(value = "用户根目录加密id")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long rootFileId;
    @ApiModelProperty(value = "用户根目录名称")
    private String rootFileName;
}
