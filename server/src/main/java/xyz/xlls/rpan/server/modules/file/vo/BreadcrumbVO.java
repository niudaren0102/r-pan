package xyz.xlls.rpan.server.modules.file.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;
import xyz.xlls.rpan.web.serializer.IdEncryptSerializer;

import java.io.Serializable;
import java.util.Objects;

@ApiModel("面包屑列表展示实体")
@Data
public class BreadcrumbVO implements Serializable {
    private static final long serialVersionUID = -3952873983876044415L;
    @ApiModelProperty("文件ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long id;
    @ApiModelProperty("父文件夹ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long parentId;
    @ApiModelProperty("文件夹名称")
    private String name;
    /**
     * 实体转换
     */
    public static BreadcrumbVO transfer(RPanUserFile record) {
        BreadcrumbVO breadcrumbVO = new BreadcrumbVO();
        if(Objects.nonNull(record)){
            breadcrumbVO.setId(record.getFileId());
            breadcrumbVO.setParentId(record.getParentId());
            breadcrumbVO.setName(record.getFilename());
        }
        return breadcrumbVO;
    }
}
