package xyz.xlls.rpan.server.modules.share.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.share.entity.RPanShare;
import xyz.xlls.rpan.server.modules.share.vo.ShareSimpleDetailVO;

import java.io.Serializable;

/**
 * 查询分享简单详情的上下文实体信息
 */
@Data
public class QueryShareSimpleDetailContext implements Serializable {
    /**
     * 分享ID
     */
    private Long shareId;
    /**
     * 分享对应的实体记录
     */
    private RPanShare record;
    /**
     * 简单分享详情的VO对象
     */
    private ShareSimpleDetailVO vo;
}
