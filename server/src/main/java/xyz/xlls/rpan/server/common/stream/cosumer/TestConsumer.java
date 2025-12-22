package xyz.xlls.rpan.server.common.stream.cosumer;

import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.server.common.stream.channel.PanChannels;
import xyz.xlls.rpan.server.common.stream.event.TestEvent;
import xyz.xlls.rpan.stream.core.AbstractConsumer;

/**
 * 测试消息消费者
 */
@Component
public class TestConsumer extends AbstractConsumer {
    /**
     * 消费测试消息
     * @param message
     */
    @StreamListener(PanChannels.TEST_INPUT)
    private void consumerTestMessage(Message<TestEvent> message) {
        printLog(message);
    }
}
