package xyz.xlls.rpan.server.modules.file.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询用户已上传的文件分片列表的参数实体
 */
@Data
public class QueryUploadedChunksContext implements Serializable {
    private static final long serialVersionUID = -549130138426284579L;
    /**
     * 文件的唯一标识
     */
    private String identifier;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
}
