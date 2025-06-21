package xyz.xlls.rpan.server.common.listener.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.event.search.UserSearchEvent;
import xyz.xlls.rpan.server.modules.user.entity.RPanUserSearchHistory;
import xyz.xlls.rpan.server.modules.user.service.IUserSearchHistoryService;

import java.util.Date;

/**
 * 用户搜事件监听其
 */
@Component
public class UserSearchEventListener {
    @Autowired
    private IUserSearchHistoryService userSearchHistoryService;
    /**
     * 监听用户搜索事件，将其保存到用户搜索的历史记录当中
     * @param event
     */
    @EventListener(classes = UserSearchEvent.class)
    public void saveSearchHistory(UserSearchEvent event){
        RPanUserSearchHistory record=new RPanUserSearchHistory();
        record.setId(IdUtil.get());
        record.setUserId(event.getUserId());
        record.setSearchContent(event.getKeyword());
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        try{
            userSearchHistoryService.save(record);
        }catch (DuplicateKeyException e){
            userSearchHistoryService.updateSearchHistoryTime(event.getUserId(), event.getKeyword());
        }
    }
}
