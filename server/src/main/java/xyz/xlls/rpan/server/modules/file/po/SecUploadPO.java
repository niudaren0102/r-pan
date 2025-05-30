package xyz.xlls.rpan.server.modules.file.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@ApiModel(value = "文件秒传参数实体")
@Data
public class SecUploadPO implements Serializable {

    private static final long serialVersionUID = -202788104532388011L;
    @ApiModelProperty(value = "父文件夹ID",required = true)
    @NotBlank(message = "父文件夹ID不能为空")
    private String parentId;
    @ApiModelProperty(value = "文件名",required = true)
    @NotBlank(message = "文件名不能为空")
    private String filename;
    @NotBlank(message = "文件的唯一标识不能为空")
    @ApiModelProperty(value = "文件的唯一标识",required = true)
    private String identifier;
}
