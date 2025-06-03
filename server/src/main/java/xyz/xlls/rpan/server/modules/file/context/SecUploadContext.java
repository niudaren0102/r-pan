package xyz.xlls.rpan.server.modules.file.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件秒传上下文实体对象
 */
@Data
public class SecUploadContext implements Serializable {

    private static final long serialVersionUID = -2933473963484902512L;
    /**
     * 文件夹ID
     */
    private Long parentId;
    /**
     * 文件名称
     */
    private String filename;
    /**
     * 文件唯一标识
     */
    private String identifier;
    /**
     * 当前登录的用户ID
     */
    private Long userId;

}
