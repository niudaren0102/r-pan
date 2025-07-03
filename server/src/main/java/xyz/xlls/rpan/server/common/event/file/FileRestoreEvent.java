package xyz.xlls.rpan.server.common.event.file;

import lombok.*;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 文件还原时间实体
 */
@EqualsAndHashCode
@ToString
@Getter
@Setter
public class FileRestoreEvent extends ApplicationEvent {
    /**
     * 被成功还原的文件记录ID集合
     */
    private List<Long> fileIdList;
    public FileRestoreEvent(Object source, List<Long> fileIdList) {
        super(source);
        this.fileIdList = fileIdList;
    }
}
