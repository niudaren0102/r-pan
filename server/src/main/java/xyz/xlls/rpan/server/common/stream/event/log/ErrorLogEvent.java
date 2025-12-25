package xyz.xlls.rpan.server.common.stream.event.log;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 错误日志事件
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ErrorLogEvent implements Serializable {
    private static final long serialVersionUID = -8250685199805331827L;
    /**
     * 错误日志内容
     */
    private String message;
    /**
     * 当前登录的用户ID
     */
    private Long userId;
    public ErrorLogEvent(String message, Long userId) {
        this.message = message;
        this.userId = userId;
    }
}
