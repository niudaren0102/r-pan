package xyz.xlls.rpan.server.modules.recycle.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel("文件还原实体")
public class RestorePO implements Serializable {
    private static final long serialVersionUID = -3582792683300220554L;
    @ApiModelProperty(value = "要还原的文件ID集合，多个使用公用分割符分割",required = true)
    @NotBlank(message = "请选择要还原的文件")
    private String fileIds;
}
