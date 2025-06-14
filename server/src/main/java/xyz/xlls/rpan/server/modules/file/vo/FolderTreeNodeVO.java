package xyz.xlls.rpan.server.modules.file.vo;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.xlls.rpan.web.serializer.IdEncryptSerializer;

import java.io.Serializable;
import java.util.List;

@ApiModel("文件树节点实体")
@Data
public class FolderTreeNodeVO implements Serializable {
    private static final long serialVersionUID = 7768699143835184049L;
    @ApiModelProperty("文件夹名称")
    private String label;
    @ApiModelProperty("文件夹ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private  Long id;
    @ApiModelProperty("父文件夹ID")
    @JsonSerialize(using = IdEncryptSerializer.class)
    private Long parentId;
    @ApiModelProperty("子节点集合")
    private List<FolderTreeNodeVO> children;
    public void print(){
        String jsonString= JSON.toJSONString(this);
        System.out.println(jsonString);
    }
}
