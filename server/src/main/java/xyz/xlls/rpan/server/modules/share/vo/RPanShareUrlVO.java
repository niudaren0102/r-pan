package xyz.xlls.rpan.server.modules.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.xlls.rpan.web.serializer.IdEncryptSerializer;

import java.io.Serializable;
@ApiModel(value = "创建分享链接的返回实体对象")
@Data
public class RPanShareUrlVO implements Serializable {
    private static final long serialVersionUID = -8923819534344750204L;
    @JsonSerialize(using = IdEncryptSerializer.class)
    @ApiModelProperty("分享的链接ID")
    private Long shareId;
    @ApiModelProperty("分享的链接名称")
    private String shareName;
    @ApiModelProperty("分享的链接的URL")
    private String shareUrl;
    @ApiModelProperty("分享的链接的提取码")
    private String shareCode;
    @ApiModelProperty("分享的链接的状态")
    private Integer shareStatus;
}
