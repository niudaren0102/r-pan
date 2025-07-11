package xyz.xlls.rpan.server.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户搜索历史表
 * @TableName r_pan_user_search_history
 */
@TableName(value ="r_pan_user_search_history")
@Data
public class RPanUserSearchHistory implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 4823958418351309305L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 搜索文案
     */
    @TableField(value = "search_content")
    private String searchContent;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;
}