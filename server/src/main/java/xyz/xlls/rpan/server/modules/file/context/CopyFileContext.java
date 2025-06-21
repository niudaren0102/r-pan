package xyz.xlls.rpan.server.modules.file.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;

import java.io.Serializable;
import java.util.List;

/**
 * 文件复制操作上下文实体对象
 */
@Data
public class CopyFileContext implements Serializable {
    private static final long serialVersionUID = -2981957154276390908L;
    /**
     * 要复制的文件ID集合
     */
    private List<Long> fileIdList;
    /**
     * 目标文件夹ID
     */
    private Long targetParentId;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
    /**
     * 要复制的文件列表
     */
    private List<RPanUserFile> prepareRecords;
 }
