package xyz.xlls.rpan.stream.core;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * 默认的消息发送实体
 */
@Component("defaultStreamProducer")
public class DefaultStreamProducer extends AbstractStreamProducer{
    @Override
    protected void afterSend(Message<Object> message, boolean result) {

    }

    @Override
    protected void preSend(Message<Object> message) {

    }
}
