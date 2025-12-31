package xyz.xlls.rpan.server.common.stream.cosumer.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.stream.channel.PanChannels;
import xyz.xlls.rpan.server.common.stream.event.search.UserSearchEvent;
import xyz.xlls.rpan.server.modules.user.entity.RPanUserSearchHistory;
import xyz.xlls.rpan.server.modules.user.service.IUserSearchHistoryService;
import xyz.xlls.rpan.stream.core.AbstractConsumer;

import java.util.Date;

/**
 * 用户搜事件监听其
 */
@Component
public class UserSearchEventConsumer extends AbstractConsumer {
    @Autowired
    private IUserSearchHistoryService userSearchHistoryService;
    /**
     * 监听用户搜索事件，将其保存到用户搜索的历史记录当中
     * @param message
     */
    @StreamListener(PanChannels.DELETE_FILE_INPUT)
    public void saveSearchHistory(Message<UserSearchEvent> message){
        if(isEmptyMessage(message)){
            return;
        }
        printLog(message);
        UserSearchEvent event = message.getPayload();
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
