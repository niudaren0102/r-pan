package xyz.xlls.rpan.server.modules.file.context;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import xyz.xlls.rpan.server.modules.file.entity.RPanFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 当文件上传的上下文实体
 */
@Data
public class FileUploadContext implements Serializable {
    private static final long serialVersionUID = 8193899470309847086L;
    /**
     * 文件名称
     */
    private String filename;
    /**
     * 文件唯一标识
     */
    private String identifier;
    /**
     * 文件总大小
     */
    private Long totalSize;
    /**
     * 文件父文件夹ID
     */
    private Long parentId;
    /**
     * 文件实体
     */
    private MultipartFile file;

    /**
     * 当前登录的用户ID
     */
    private Long userId;
    /**
     * 实体文件记录
     */
    private RPanFile record;
}
