package org.ssoggy.ssoggysouls.util;

import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SchedulerManager {
    
    private SchedulerManager() {
        // Utility class
    }
    
    private static final Map<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private static int taskIdCounter = 0;

    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onServerTick(ServerTickEvent.Post event) {
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
                    task.runnable.run();
                } catch (Exception e) {
                    SSoggySoulsMod.LOGGER.error("Error executing scheduled task", e);
                }
                
                if (task.periodTicks > 0) {
                    task.delayTicks = task.periodTicks - 1;
                } else {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Runs a task after a delay.
     * @param runnable The task to run.
     * @param delayTicks Delay in ticks.
     * @return The scheduled task instance.
     */
    public static ScheduledTask runLater(Runnable runnable, int delayTicks) {
        ScheduledTask task = new ScheduledTask(taskIdCounter++, runnable, delayTicks, 0);
        tasks.put(task.taskId, task);
        return task;
    }

    /**
     * Runs a repeating task.
     * @param runnable The task to run.
     * @param delayTicks Initial delay in ticks.
     * @param periodTicks Delay between executions in ticks.
     * @return The scheduled task instance.
     */
    public static ScheduledTask runTimer(Runnable runnable, int delayTicks, int periodTicks) {
        ScheduledTask task = new ScheduledTask(taskIdCounter++, runnable, delayTicks, periodTicks);
        tasks.put(task.taskId, task);
        return task;
    }
    
    /**
     * Cancels a scheduled task by ID.
     */
    public static void cancelTask(int taskId) {
        ScheduledTask task = tasks.get(taskId);
        if (task != null) {
            task.cancel();
        }
    }

    public static class ScheduledTask {
        private final int taskId;
        private final Runnable runnable;
        private int delayTicks;
        private final int periodTicks;
        private boolean cancelled = false;

        public ScheduledTask(int taskId, Runnable runnable, int delayTicks, int periodTicks) {
            this.taskId = taskId;
            this.runnable = runnable;
            this.delayTicks = delayTicks;
            this.periodTicks = periodTicks;
        }

        public int getTaskId() {
            return taskId;
        }

        public void cancel() {
            this.cancelled = true;
        }
        
        public boolean isCancelled() {
            return cancelled;
        }
    }
}
