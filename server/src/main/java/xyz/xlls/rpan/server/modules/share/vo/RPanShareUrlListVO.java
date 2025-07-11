package xyz.xlls.rpan.server.modules.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.xlls.rpan.web.serializer.Date2StringSerializer;
import xyz.xlls.rpan.web.serializer.IdEncryptSerializer;

import java.io.Serializable;
import java.util.Date;

@ApiModel("分享链接列表结果实体对象")
@Data
public class RPanShareUrlListVO implements Serializable {
    private static final long serialVersionUID = -6624144258833195778L;
    @ApiModelProperty("分享的ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long shareId;
    @ApiModelProperty("分享的名称")
    private String shareName;
    @ApiModelProperty("分享的URL")
    private String shareUrl;
    @ApiModelProperty("分享的分享码")
    private String shareCode;
    @ApiModelProperty("分享的状态")
    private Integer shareStatus;
    @ApiModelProperty("分享的类型")
    private Integer shareType;
    @ApiModelProperty("分享的过期类型")
    private Integer shareDayType;
    @ApiModelProperty("分享的过期时间")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date shareEndTime;
    @ApiModelProperty("分享的创建事件")
    @JsonSerialize(using = Date2StringSerializer.class)
    private Date createTime;
}
