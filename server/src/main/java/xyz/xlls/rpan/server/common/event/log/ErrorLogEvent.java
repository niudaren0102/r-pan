package xyz.xlls.rpan.server.common.event.log;

import lombok.*;
import org.springframework.context.ApplicationEvent;

/**
 * 错误日志事件
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ErrorLogEvent extends ApplicationEvent {
    /**
     * 错误日志内容
     */
    private String message;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
    public ErrorLogEvent(Object source, String message, Long userId) {
        super(source);
        this.message = message;
        this.userId = userId;
    }
}
