package xyz.xlls.rpan.server.modules.test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.xlls.rpan.core.response.R;
import xyz.xlls.rpan.server.common.annotation.LoginIgnore;
import xyz.xlls.rpan.server.common.stream.channel.PanChannels;
import xyz.xlls.rpan.server.common.stream.event.TestEvent;
import xyz.xlls.rpan.stream.core.IStreamProducer;

/**
 * 测试处理器
 */
@RestController
public class TestController implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Autowired
    @Qualifier(value = "defaultStreamProducer")
    private IStreamProducer producer;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 测试流事件发布
     * @return
     */
    @GetMapping("stream/test")
    @LoginIgnore
    public R streamTest(String name) {
        TestEvent testEvent = new TestEvent();
        testEvent.setName(name);
        producer.sendMessage(PanChannels.TEST_OUTPUT,testEvent);
        return R.success();
    }
}
