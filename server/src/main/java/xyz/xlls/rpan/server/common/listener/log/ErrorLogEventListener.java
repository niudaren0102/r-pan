package xyz.xlls.rpan.server.common.listener.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.core.utils.IdUtil;
import xyz.xlls.rpan.server.common.event.log.ErrorLogEvent;
import xyz.xlls.rpan.server.modules.log.entity.RPanErrorLog;
import xyz.xlls.rpan.server.modules.log.service.IErrorLogService;

import java.util.Date;

/**
 * 系统错误日志监听器
 */
@Component
public class ErrorLogEventListener {
    @Autowired
    private IErrorLogService errorLogService;
    /**
     * 监听系统错误日志时间，并保存到数据库中
     * @param event
     */
    @EventListener(ErrorLogEvent.class)
    private void saveErrorLog(ErrorLogEvent event){
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
