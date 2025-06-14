package xyz.xlls.rpan.server.modules.file.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询文件夹树的上下文实体信息
 */
@Data
public class QueryFolderTreeContext implements Serializable {
    private static final long serialVersionUID = 2784976500577803013L;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
}
