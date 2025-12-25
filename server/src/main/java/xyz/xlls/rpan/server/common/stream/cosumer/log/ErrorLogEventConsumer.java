package xyz.xlls.rpan.server.common.stream.cosumer.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.stream.channel.PanChannels;
import xyz.xlls.rpan.server.common.stream.event.log.ErrorLogEvent;
import xyz.xlls.rpan.server.modules.log.entity.RPanErrorLog;
import xyz.xlls.rpan.server.modules.log.service.IErrorLogService;
import xyz.xlls.rpan.stream.core.AbstractConsumer;

import java.util.Date;

/**
 * 系统错误日志监听器
 */
@Component
public class ErrorLogEventConsumer extends AbstractConsumer {
    @Autowired
    private IErrorLogService errorLogService;
    /**
     * 监听系统错误日志时间，并保存到数据库中
     * @param message
     */
    @StreamListener(PanChannels.ERROR_LOG_INPUT)
    public void saveErrorLog(Message<ErrorLogEvent> message){
        if(isEmptyMessage(message)){
            return;
        }
        printLog(message);
        ErrorLogEvent event = message.getPayload();
        RPanErrorLog record=new RPanErrorLog();
        record.setId(IdUtil.get());
        record.setLogContent(event.getMessage());
        record.setLogStatus(0);
        record.setCreateUser(event.getUserId());
        record.setCreateTime(new Date());
        record.setUpdateUser(event.getUserId());
        record.setUpdateTime(new Date());
        errorLogService.save(record);
    }
}
