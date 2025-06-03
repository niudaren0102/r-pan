package xyz.xlls.rpan.server.modules.file.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel(value = "单文件上传参数实体对象")
@Data
public class FileUploadPO implements Serializable {
    private static final long serialVersionUID = 242535801645702053L;
    @ApiModelProperty(value = "文件名称",required = true)
    @NotBlank(message = "文件名称不能为空")
    private String filename;
    @ApiModelProperty(value = "文件唯一标识",required = true)
    @NotBlank(message = "文件唯一标识不能为空")
    private String identifier;
    @ApiModelProperty(value = "文件总大小",required = true)
    @NotNull(message = "文件总大小不能为空")
    private Long totalSize;
    @ApiModelProperty(value = "文件父文件夹ID",required = true)
    @NotBlank(message = "文件父文件夹ID不能为空")
    private String parentId;
    @ApiModelProperty(value = "文件实体",required = true)
    @NotNull(message = "文件实体不能为空")
    private MultipartFile file;

}
