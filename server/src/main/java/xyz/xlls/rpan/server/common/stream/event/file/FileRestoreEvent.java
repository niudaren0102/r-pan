package xyz.xlls.rpan.server.common.stream.event.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 文件还原时间实体
 */
@EqualsAndHashCode
@ToString
@Getter
@Setter
public class FileRestoreEvent implements Serializable {
    private static final long serialVersionUID = 1685125845822579221L;
    /**
     * 被成功还原的文件记录ID集合
     */
    private List<Long> fileIdList;
    public FileRestoreEvent(List<Long> fileIdList) {

        this.fileIdList = fileIdList;
    }
}
