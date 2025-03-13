package xyz.xlls.rpan.server.modules.file.context;

import lombok.Data;
import java.io.Serializable;

/**
 * 创建文件夹上下文实体
 */
@Data
public class CreateFolderContext implements Serializable {
    private static final long serialVersionUID = -543504842289994613L;
    /**
     * 父文件夹ID
     */
    private Long parentId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 文件夹名称
     */
    private String folderName;
}
