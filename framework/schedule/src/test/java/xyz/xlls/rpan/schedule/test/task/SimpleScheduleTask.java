package xyz.xlls.rpan.schedule.test.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.xlls.rpan.schedule.ScheduleTask;

/**
 * 简单的定时任务执行逻辑
 */
@Component
@Slf4j
public class SimpleScheduleTask implements ScheduleTask {
    @Override
    public String getName() {
        return "测试定时任务";
    }

    @Override
    public void run() {
        log.info(getName()+"正在执行。。。");
    }
}
