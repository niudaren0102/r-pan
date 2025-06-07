package xyz.xlls.rpan.storage.engine.core.context;

import lombok.Data;

import java.io.InputStream;
import java.io.Serializable;

/**
 * 保存文件分片记录的上下文信息
 */
@Data
public class StoreFileChunkContext implements Serializable {
    private static final long serialVersionUID = 5401209519870186275L;
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
     * 文件输入流信息
     */
    private InputStream inputStream;
    /**
     * 文件的真实存储路径
     */
    private String realPath;
    /**
     * 文件的总分片数
     */
    private Integer totalChunks;
    /**
     * 文件当前分片下标
     */
    private Integer chunkNumber;
    /**
     * 】当前分片大小
     */
    private Long currentChunkSize;
    /**
     * 当前登录用户id
     */
    private Long userId;
}
