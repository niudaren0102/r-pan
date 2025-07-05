package xyz.xlls.rpan.server.modules.share.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
@ApiModel("检验分享码参数实体")
@Data
public class CheckShareCodePO implements Serializable {
    private static final long serialVersionUID = -1652174726576921474L;
    @ApiModelProperty(value = "分享的ID",required = true)
    @NotBlank(message = "分享的ID不能为空")
    private String shareId;
    @ApiModelProperty(value = "分享码",required = true)
    @NotBlank(message = "分享码不能为空")
    private String shareCode;
}
