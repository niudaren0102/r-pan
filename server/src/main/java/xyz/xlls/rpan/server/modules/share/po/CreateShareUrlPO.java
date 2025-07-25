package xyz.xlls.rpan.server.modules.share.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "创建分享链接参数对象实体")
@Data
public class CreateShareUrlPO implements Serializable {
    private static final long serialVersionUID = 8062879666870278331L;
    @ApiModelProperty(value = "分享的名称",required = true)
    @NotBlank(message = "分享名称不能为空")
    private String shareName;
    @ApiModelProperty(value = "分享的类型",required = true)
    @NotNull(message = "分享的类型不能为空")
    private Integer shareType;
    @ApiModelProperty(value = "分享的日期类型",required = true)
    @NotNull(message = "分享的日期类型不能为空")
    private Integer shareDayType;
    @ApiModelProperty(value = "分享的文件ID集合，多个使用公用的分隔符去拼接",required = true)
    @NotBlank(message = "分享的文件ID集合不能为空")
    private String shareFileIds;
}
