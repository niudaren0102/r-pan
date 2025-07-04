package xyz.xlls.rpan.server.modules.share.service;

import xyz.xlls.rpan.server.modules.share.context.CreateShareUrlContext;
import xyz.xlls.rpan.server.modules.share.context.QueryShareUrlListContext;
import xyz.xlls.rpan.server.modules.share.entity.RPanShare;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.xlls.rpan.server.modules.share.vo.RPanShareUrlListVO;
import xyz.xlls.rpan.server.modules.share.vo.RPanShareUrlVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【r_pan_share(用户分享表)】的数据库操作Service
* @createDate 2024-10-22 15:08:03
*/
public interface IShareService extends IService<RPanShare> {
    /**
     * 创建分享链接
     * @param context
     * @return
     */
    RPanShareUrlVO create(CreateShareUrlContext context);

    /**
     * 查询用户的分享列表
     * @param context
     * @return
     */
    List<RPanShareUrlListVO> getShares(QueryShareUrlListContext context);
}
