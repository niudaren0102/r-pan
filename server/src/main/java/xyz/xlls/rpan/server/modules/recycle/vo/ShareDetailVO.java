package xyz.xlls.rpan.server.modules.recycle.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@ApiModel("分享详情的返回实体对象")
@Data
public class ShareDetailVO implements Serializable {
    private Long shareId;
    private String shareName;
    private Date createTime;
    private Date u
}
