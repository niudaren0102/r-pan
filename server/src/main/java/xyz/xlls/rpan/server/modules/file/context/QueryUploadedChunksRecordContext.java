package xyz.xlls.rpan.server.modules.file.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询用户分片实体记录列表的上下文对象
 */
@Data
public class QueryUploadedChunksRecordContext implements Serializable {
    private static final long serialVersionUID = -3203145502539476401L;
    /**
     * 文件的唯一标识
     */
    private String identifier;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
}
