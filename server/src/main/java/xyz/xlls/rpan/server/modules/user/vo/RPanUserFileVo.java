package xyz.xlls.rpan.server.modules.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.xlls.rpan.web.serializer.IdEncryptSerializer;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户查询文件列表响应实体
 */
@Data
@ApiModel(value = "文件列表相应实体")
public class RPanUserFileVo implements Serializable {

    private static final long serialVersionUID = -3949745217973043546L;
    @ApiModelProperty(value = "文件ID")
    @JsonSerialize(using= IdEncryptSerializer.class)
    private Long fileId;
    @ApiModelProperty(value = "父文件夹ID")
    @JsonSerialize(using= IdEncryptSerializer.class)
    private Long parentId;
    @ApiModelProperty(value = "文件名称")
    private String fileName;
    @ApiModelProperty(value = "文件大小描述")
    private String fileSizeDesc;
    @ApiModelProperty(value = "文件类标识 0否 1是")
    private Integer folderFlag;
    @ApiModelProperty(value = "文件类型 1 普通文件 2 压缩文件 3 excel 4 word 5 pdf 6 txt 7 图片 8 音频 9 视频 10 ppt 11 源码文件 12 csv")
    private String fileType;
    @ApiModelProperty(value = "文件更新时间")
    private Date updateTime;

}
