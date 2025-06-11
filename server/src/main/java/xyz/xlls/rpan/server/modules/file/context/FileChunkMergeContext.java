package xyz.xlls.rpan.server.modules.file.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.file.entity.RPanFile;

import java.io.Serializable;

/**
 * 合并并保存物理记录的上下文实体对象
 */
@Data
public class FileChunkMergeContext implements Serializable {

    private static final long serialVersionUID = -3535676005487894581L;
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
     * 文件的父文件夹ID
     */
    private Long parentId;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
    /**
     * 物理文件记录
     */
    private RPanFile record;
}
