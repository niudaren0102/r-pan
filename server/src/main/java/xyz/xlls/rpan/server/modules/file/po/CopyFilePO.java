package xyz.xlls.rpan.server.modules.file.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@ApiModel(value = "文件复制实体对象")
@Data
public class CopyFilePO implements Serializable {

    private static final long serialVersionUID = -2619073924962465696L;
    @ApiModelProperty(value = "要复制的文件ID集合，多个使用公用分隔符隔开")
    @NotBlank(message = "请选择需要复制的文件")
    private String fileIds;
    @ApiModelProperty(value = "要复制到的目标文件夹的ID")
    @NotBlank(message = "请选择需要复制到那个文件加下面")
    private String targetParentId;
}
