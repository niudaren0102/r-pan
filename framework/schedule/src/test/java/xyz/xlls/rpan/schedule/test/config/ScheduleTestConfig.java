package xyz.xlls.rpan.schedule.test.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import xyz.xlls.rpan.core.constants.RPanConstants;

/**
 * 单元测试配置类
 */
@SpringBootConfiguration
@ComponentScan(RPanConstants.BASE_COMPONENT_SCAN_PATH+".schedule")
public class ScheduleTestConfig {
}
