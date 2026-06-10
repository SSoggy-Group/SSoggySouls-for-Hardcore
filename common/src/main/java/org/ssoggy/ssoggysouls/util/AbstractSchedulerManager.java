package org.ssoggy.ssoggysouls.util;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class AbstractSchedulerManager {

    // ⚡ Bolt: Using ConcurrentSkipListMap for O(log N) cancellation lookups while maintaining FIFO execution order based on incremental taskId
    protected static final Map<Integer, ScheduledTask> tasks = new ConcurrentSkipListMap<>();
    protected static final AtomicInteger taskIdCounter = new AtomicInteger(0);

    protected static void tickTasks(Consumer<Exception> errorHandler) {
        Iterator<ScheduledTask> iterator = tasks.values().iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();

            if (task.cancelled) {
                iterator.remove();
                continue;
            }

            if (task.delayTicks > 0) {
                task.delayTicks--;
            } else {
                try {
                    task.getRunnable().run();
                } catch (Exception e) {
                    if (errorHandler != null) {
                        errorHandler.accept(e);
                    }
                }

                if (task.periodTicks > 0) {
                    task.delayTicks = task.periodTicks - 1;
                } else {
                    iterator.remove();
                }
            }
        }
    }

    public static ScheduledTask runLater(Runnable runnable, int delayTicks) {
        int id = taskIdCounter.getAndIncrement();
        ScheduledTask task = new ScheduledTask(id, runnable, delayTicks, 0);
        tasks.put(id, task);
        return task;
    }

    public static ScheduledTask runTimer(Runnable runnable, int delayTicks, int periodTicks) {
        int id = taskIdCounter.getAndIncrement();
        ScheduledTask task = new ScheduledTask(id, runnable, delayTicks, periodTicks);
        tasks.put(id, task);
        return task;
    }

    public static void cancelTask(int taskId) {
        // ⚡ Bolt: O(log N) cancellation lookup without iterating through entire task list
        ScheduledTask task = tasks.get(taskId);
        if (task != null) {
            task.cancel();
        }
    }
}
