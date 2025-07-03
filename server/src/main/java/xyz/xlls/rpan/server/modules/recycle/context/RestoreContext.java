package xyz.xlls.rpan.server.modules.recycle.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;

import java.io.Serializable;
import java.util.List;

/**
 * 还原上下文的实体对象
 */
@Data
public class RestoreContext implements Serializable {
    private static final long serialVersionUID = -9165028120062585627L;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
    /**
     * 要操作的文件ID列表
     */
    private List<Long> fileIdList;
    /**
     * 要被还原的文件记录列表
     */
    private List<RPanUserFile> records;
}
