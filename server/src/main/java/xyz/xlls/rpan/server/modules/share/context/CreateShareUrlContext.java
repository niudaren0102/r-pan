package xyz.xlls.rpan.server.modules.share.context;

import lombok.Data;
import xyz.xlls.rpan.server.modules.share.entity.RPanShare;

import java.io.Serializable;
import java.util.List;

/**
 * 创建分享链接上下文实体对象
 */
@Data
public class CreateShareUrlContext implements Serializable {
    private static final long serialVersionUID = 3153552131555815540L;
    /**
     * 分享名称
     */
    private String shareName;
    /**
     * 分享的类型
     */
    private Integer shareType;
    /**
     * 分享的日期类型
     */
    private Integer shareDayType;
    /**
     * 分享的文件ID集合
     */
    private List<Long> shareFileIdList;
    /**
     * 当前登录用户ID
     */
    private Long userId;
    /**
     * 已经保存的分享记录实体
     */
    private RPanShare record;
}
