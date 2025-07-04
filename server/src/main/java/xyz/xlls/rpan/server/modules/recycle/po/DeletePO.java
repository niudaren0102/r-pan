package xyz.xlls.rpan.server.modules.recycle.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
@ApiModel("文件彻底删除参数实体")
@Data
public class DeletePO implements Serializable {
    private static final long serialVersionUID = 3489031837228377682L;
    @ApiModelProperty(value = "要删除的文件ID集合，多个使用公用分割符分割",required = true)
    @NotBlank(message = "请选择要删除的文件")
    private String fileIds;
}
