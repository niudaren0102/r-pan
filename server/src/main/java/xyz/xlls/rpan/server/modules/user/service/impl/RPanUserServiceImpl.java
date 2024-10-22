package xyz.xlls.rpan.server.modules.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.xlls.rpan.server.modules.user.entity.RPanUser;
import xyz.xlls.rpan.server.modules.user.service.RPanUserService;
import xyz.xlls.rpan.server.modules.user.mapper.RPanUserMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【r_pan_user(用户信息表)】的数据库操作Service实现
* @createDate 2024-10-22 14:59:10
*/
@Service
public class RPanUserServiceImpl extends ServiceImpl<RPanUserMapper, RPanUser>
    implements RPanUserService{

}




