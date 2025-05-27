package xyz.xlls.rpan.server.modules.user.mapper;

import org.apache.ibatis.annotations.Param;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Administrator
* @description 针对表【r_pan_user(用户信息表)】的数据库操作Mapper
* @createDate 2024-10-22 14:59:10
* @Entity xyz.xlls.rpan.server.modules.user.entity.RPanUser
*/
public interface RPanUserMapper extends BaseMapper<RPanUser> {
    /**
     * 通过用户名称查询用户设置的用户密保问题
     * @param username
     * @return
     */
    String selectQuestionByUsername(@Param("username") String username);
}