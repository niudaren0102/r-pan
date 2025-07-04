package xyz.xlls.rpan.server.modules.share.context;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 保存文件和分享的关联关系上下文实体对象
 */
@Data
public class SaveShareFilesContext implements Serializable {
    private static final long serialVersionUID = -8040272616081558952L;
    /**
     * 分享id
     */
    private Long shareId;
    /**
     * 分享的文件ID列表
     */
    private List<Long> shareFileIdList;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
}
