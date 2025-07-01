package xyz.xlls.rpan.server.modules.recycle.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询用户回收站列表上下文实体对象
 */
@Data
public class QueryRecycleFileListContext implements Serializable {
    private static final long serialVersionUID = -7540572564644000137L;
    /**
     * 用户ID
     */
    private Long userId;
}
