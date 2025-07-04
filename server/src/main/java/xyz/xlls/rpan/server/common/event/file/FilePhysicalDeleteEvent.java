package xyz.xlls.rpan.server.common.event.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;

import java.util.List;

/**
 * 文件被物理删除的事件实体
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class FilePhysicalDeleteEvent extends ApplicationEvent {
    /**
     * 所有被删除的文件实体集合
     */
    private List<RPanUserFile> allRecords;

    public FilePhysicalDeleteEvent(Object source, List<RPanUserFile> allRecords) {
        super(source);
        this.allRecords = allRecords;
    }
}
