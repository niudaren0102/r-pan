package xyz.xlls.rpan.server.common.stream.event.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.io.Serializable;
import java.util.List;

/**
 * 文件删除事件
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DeleteFileEvent implements Serializable {
    private static final long serialVersionUID = 3314312721922968835L;
    private List<Long> fileIdList;

    public DeleteFileEvent(List<Long> fileIdList) {
        this.fileIdList=fileIdList;
    }
}
