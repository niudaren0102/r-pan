package xyz.xlls.rpan.server.modules.share.context;

import lombok.Data;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * 分享文件下载上下文实体对象
 */
@Data
public class ShareFileDownloadContext implements Serializable {
    private static final long serialVersionUID = 6399879709926739544L;
    /**
     * 要下载的文件ID
     */
    private Long fileId;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
    /**
     * 分享ID
     */
    private Long shareId;
    /**
     * 响应实体
     */
    private HttpServletResponse response;
}
