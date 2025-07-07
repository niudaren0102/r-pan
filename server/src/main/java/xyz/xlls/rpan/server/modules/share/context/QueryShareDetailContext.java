package xyz.xlls.rpan.server.modules.share.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.share.entity.RPanShare;
import xyz.xlls.rpan.server.modules.share.vo.ShareDetailVO;

import java.io.Serializable;

/**
 * 查询分享详情的上下文实体对象
 */
@Data
public class QueryShareDetailContext implements Serializable {
    private static final long serialVersionUID = -4132712455926379454L;
    /**
     * 对应的分享ID
     */
    private Long shareId;
    /**
     * 分享实体
     */
    private RPanShare record;
    /**
     * 分享详情的VO对象
     */
    private ShareDetailVO vo;
}
