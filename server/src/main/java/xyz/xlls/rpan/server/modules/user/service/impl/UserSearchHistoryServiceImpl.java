package xyz.xlls.rpan.server.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.xlls.rpan.server.modules.user.entity.RPanUserSearchHistory;
import xyz.xlls.rpan.server.modules.user.service.IUserSearchHistoryService;
import xyz.xlls.rpan.server.modules.user.mapper.RPanUserSearchHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author Administrator
* @description 针对表【r_pan_user_search_history(用户搜索历史表)】的数据库操作Service实现
* @createDate 2024-10-22 14:59:10
*/
@Service
public class UserSearchHistoryServiceImpl extends ServiceImpl<RPanUserSearchHistoryMapper, RPanUserSearchHistory>
    implements IUserSearchHistoryService {

    @Override
    public void updateSearchHistoryTime(Long userId, String searchContent) {
        LambdaUpdateWrapper<RPanUserSearchHistory> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.eq(RPanUserSearchHistory::getUserId,userId);
        updateWrapper.eq(RPanUserSearchHistory::getSearchContent,searchContent);
        updateWrapper.set(RPanUserSearchHistory::getUpdateTime,new Date());
        this.update(updateWrapper);
    }
}




