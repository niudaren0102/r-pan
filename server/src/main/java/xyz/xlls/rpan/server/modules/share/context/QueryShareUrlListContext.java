package xyz.xlls.rpan.server.modules.share.context;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询用户已有的分享链接列表的上下文实体对象
 */
@Data
public class QueryShareUrlListContext implements Serializable {
    private static final long serialVersionUID = -1898511729695162042L;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
}
