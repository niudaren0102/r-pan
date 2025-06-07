package xyz.xlls.rpan.server.modules.file.context;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import xyz.xlls.rpan.server.modules.file.enums.MergeFlagEnum;

import java.io.Serializable;

/**
 * 文件分片保存的上下文实体信息
 */
@Data
public class FileChunkSaveContext implements Serializable {

    private static final long serialVersionUID = 789053793509064081L;
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
    /**
     * 是否需要合并文件 0 不需要 1 需要
     */
    private MergeFlagEnum mergeFlagEnum=MergeFlagEnum.NOT_READY;
    /**
     * 文件分片的真实存储路径
     */
    private String realPath;
}
