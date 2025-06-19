package xyz.xlls.rpan.server.modules.file.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@ApiModel(value = "文件参数转移实体对象")
@Data
public class TransferFilePO implements Serializable {

    private static final long serialVersionUID = -2583566714250215325L;
    @ApiModelProperty(value = "要转移的文件ID集合，多个使用公用分隔符隔开")
    @NotBlank(message = "请选择需要转移的文件")
    private String fileIds;
    @ApiModelProperty(value = "要转移到的目标文件夹的ID")
    @NotBlank(message = "请选择需要转移到那个文件加下面")
    private String targetParentId;
}
