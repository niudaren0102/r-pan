package xyz.xlls.rpan.server.modules.user.context;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户查询文件列表上下文实体
 */
@Data
public class QueryFileContext implements Serializable {
    private static final long serialVersionUID = -3039828638307088559L;
    /**
     * 文件父ID
     */
    private Long parentId;
    /**
     * 文件类型的集合
     */
    private List<Integer> fileTypeArray;
    /**
     * 当前登录用户
     */
    private Long userId;
    /**
     * 文件的删除标识
     */
    private Integer delFlag;

}
