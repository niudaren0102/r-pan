package xyz.xlls.rpan.server.common.stream.event.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import xyz.xlls.rpan.server.modules.file.entity.RPanUserFile;

import java.io.Serializable;
import java.util.List;

/**
 * 文件被物理删除的事件实体
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class FilePhysicalDeleteEvent implements Serializable {
    private static final long serialVersionUID = -1442451778235833109L;
    /**
     * 所有被删除的文件实体集合
     */
    private List<RPanUserFile> allRecords;

    public FilePhysicalDeleteEvent(List<RPanUserFile> allRecords) {
        this.allRecords = allRecords;
    }
}
