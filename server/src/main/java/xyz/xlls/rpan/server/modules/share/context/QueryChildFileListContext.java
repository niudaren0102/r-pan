package xyz.xlls.rpan.server.modules.share.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.share.entity.RPanShare;

import java.io.Serializable;

/**
 * 查询下一级文件列表的上下文实体信息
 */
@Data
public class QueryChildFileListContext implements Serializable {
    private static final long serialVersionUID = 3441749878636442603L;
    /**
     * 分享的ID
     */
    private Long shareId;
    /**
     * 父文件夹的ID
     */
    private Long parentId;
    /**
     * 分享对应的实体信息
     */
    private RPanShare record;

}
