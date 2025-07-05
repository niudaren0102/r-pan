package xyz.xlls.rpan.server.modules.share.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.share.entity.RPanShare;

import java.io.Serializable;

/**
 * 校验分享码上下文实体对象
 */
@Data
public class CheckShareCodeContext implements Serializable {
    private static final long serialVersionUID = 6944920858127360972L;
    /**
     * 分享ID
     */
    private Long shareId;
    /**
     * 分享码
     */
    private String shareCode;
    /**
     * 对应的分享实体
     */
    private RPanShare record;
}
