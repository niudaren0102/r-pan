package xyz.xlls.rpan.server.modules.file.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 文件重命名参数对象
 */
@ApiModel(value = "文件重命名参数对象")
@Data
public class UpdateFilenamePO implements Serializable {
    private static final long serialVersionUID = -2001508367381465398L;
    @ApiModelProperty(value = "更新的文件ID",required = true)
    @NotBlank
    private String fileId;
    @ApiModelProperty(value = "新的文件名",required = true)
    @NotBlank
    private String newFilename;

}
