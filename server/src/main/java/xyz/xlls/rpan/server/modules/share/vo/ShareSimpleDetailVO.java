package xyz.xlls.rpan.server.modules.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.xlls.rpan.web.serializer.IdEncryptSerializer;

import java.io.Serializable;
@ApiModel("查询分享简单想起返回实体对象")
@Data
public class ShareSimpleDetailVO implements Serializable {
    private static final long serialVersionUID = -9135336719205665100L;
    @ApiModelProperty("分享ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long shareId;
    @ApiModelProperty("分享名称")
    private String shareName;
    @ApiModelProperty("分享人的信息")
    private ShareUserInfoVO shareUserInfoVO;
}
