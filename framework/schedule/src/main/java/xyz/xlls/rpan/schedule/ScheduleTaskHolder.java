package xyz.xlls.rpan.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ScheduledFuture;

/**
 * 定时任务和定时任务结果的缓存对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleTaskHolder {
    /**
     * 执行任务实体
     */
    private ScheduleTask scheduleTask;
    /**
     * 执行任务结果实体
     */
    private ScheduledFuture scheduledFuture;
}
