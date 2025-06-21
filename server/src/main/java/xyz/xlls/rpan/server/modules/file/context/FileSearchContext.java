package xyz.xlls.rpan.server.modules.file.context;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索文件上下文实体信息
 */
@Data
public class FileSearchContext implements Serializable {
    private static final long serialVersionUID = -495009665946283470L;
    /**
     * 搜索关键字
     */
    private String keyword;
    /**
     * 搜索的文件类型集合
     */
    private List<Integer> fileTypeArray;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
    /**
     * 文件的删除标识
     */
    private Integer delFlag;
}
