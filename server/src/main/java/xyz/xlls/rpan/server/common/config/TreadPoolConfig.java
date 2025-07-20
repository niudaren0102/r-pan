package xyz.xlls.rpan.server.common.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 */
@SpringBootConfiguration
public class TreadPoolConfig {
    @Bean(name="eventListenerTaskExecutor")
    public ThreadPoolTaskExecutor eventListenerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setKeepAliveSeconds(200);
        executor.setQueueCapacity(2048);
        executor.setThreadNamePrefix("event-listener-thread");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

}
