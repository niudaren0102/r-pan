package xyz.xlls.rpan.server.modules.file.context;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 文件分片上传上下文实体
 */
@Data
public class FileChunkUploadContext implements Serializable {

    private static final long serialVersionUID = -7357875444450437844L;
    /**
     * 文件名称
     */
    private String filename;
    /**
     * 文件唯一标识
     */
    private String identifier;
    /**
     * 文件总体分片数
     */
    private Integer totalChunks;
    /**
     * 当前分片下标
     * 从1开始
     */
    private Integer chunkNumber;
    /**
     * 当前分片大小
     */
    private Long currentChunkSize;
    /**
     * 文件总大小
     */
    private Long totalSize;
    /**
     * 分片文件实体
     */
    private MultipartFile file;
    /**
     * 当前登录用户ID
     */
    private Long userId;
}
