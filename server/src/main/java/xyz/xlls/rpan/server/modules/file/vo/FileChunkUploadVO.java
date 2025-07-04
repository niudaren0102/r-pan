package xyz.xlls.rpan.server.modules.file.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("文件分片上传的响应实体")
@Data
public class FileChunkUploadVO implements Serializable {
    private static final long serialVersionUID = -7621474992108241433L;
    @ApiModelProperty(value = "是否需要合并文件 0 不需要 1 需要")
    private Integer mergeFlag;
}
