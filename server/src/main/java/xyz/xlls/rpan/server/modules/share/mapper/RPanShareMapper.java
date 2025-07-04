package xyz.xlls.rpan.server.modules.share.mapper;

import org.apache.ibatis.annotations.Param;
import xyz.xlls.rpan.server.modules.share.entity.RPanShare;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xyz.xlls.rpan.server.modules.share.vo.RPanShareUrlListVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【r_pan_share(用户分享表)】的数据库操作Mapper
* @createDate 2024-10-22 15:08:03
* @Entity xyz.xlls.rpan.server.modules.share.entity.RPanShare
*/
public interface RPanShareMapper extends BaseMapper<RPanShare> {
    /**
     * 查询用户的分享列表
     * @param userId
     * @return
     */
    List<RPanShareUrlListVO> selectShareVOListByUserId(@Param("userId") Long userId);
}




