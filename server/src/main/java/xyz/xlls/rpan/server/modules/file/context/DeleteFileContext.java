package xyz.xlls.rpan.server.modules.file.context;

import lombok.Data;

import java.util.List;

/**
 * 批量删除文件上下文实体对象
 */
@Data
public class DeleteFileContext {
    /**
     * 要删除的文件ID列表
     */
    private List<Long> fileIdList;
    /**
     * 当前的登录用户id
     */
    private Long userId;
}
