package xyz.xlls.rpan.schedule.test;

import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import xyz.xlls.rpan.schedule.ScheduleManager;
import xyz.xlls.rpan.schedule.test.config.ScheduleTestConfig;
import xyz.xlls.rpan.schedule.test.task.SimpleScheduleTask;

/**
 * 定时任务的单元测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScheduleTestConfig.class)
public class ScheduleTaskTesk {
    @Autowired
    private ScheduleManager manager;

    @Autowired
    private SimpleScheduleTask scheduleTask;

    @Test
    public void testRunScheduleTask() throws InterruptedException {
        String cron="0/5 * * * * ? ";
        String key = manager.startTask(scheduleTask, cron);
        Thread.sleep(10000);
        cron="0/1 * * * * ? ";
        key=manager.changeTask(key,cron);
        Thread.sleep(10000);
        manager.stopTask(key);
    }
}
