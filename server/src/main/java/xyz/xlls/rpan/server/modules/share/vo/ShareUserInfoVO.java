package xyz.xlls.rpan.server.modules.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.xlls.rpan.web.serializer.IdEncryptSerializer;

import java.io.Serializable;

@ApiModel("分享者信息返回实体对象")
@Data
public class ShareUserInfoVO implements Serializable {
    private static final long serialVersionUID = 2302725902804785317L;
    @ApiModelProperty("分享者的ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long userId;
    @ApiModelProperty("分享者的名称")
    private String username;
}
