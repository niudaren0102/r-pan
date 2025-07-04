package xyz.xlls.rpan.server.modules.recycle.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;

import java.io.Serializable;
import java.util.List;

/**
 * 文件彻底删除上下文的实体对象
 */
@Data
public class DeleteContext implements Serializable {
    private static final long serialVersionUID = -4909614799731846614L;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
    /**
     * 要操作的文件ID列表
     */
    private List<Long> fileIdList;
    /**
     * 要被删除的文件记录列表
     */
    private List<RPanUserFile> records;
    /**
     * 所有需要删除的文件记录列表
     */
    private List<RPanUserFile> allRecords;
}
