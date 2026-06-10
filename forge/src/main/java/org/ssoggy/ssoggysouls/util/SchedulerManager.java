package org.ssoggy.ssoggysouls.util;

import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = SSoggySoulsMod.MODID)
public class SchedulerManager {
    
    private SchedulerManager() {
        // Utility class
    }
    
    // ⚡ Bolt: Using ConcurrentHashMap instead of ConcurrentLinkedQueue for O(1) task cancellation lookups
    private static final Map<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private static final AtomicInteger taskIdCounter = new AtomicInteger(0);

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
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
        int id = taskIdCounter.getAndIncrement();
        ScheduledTask task = new ScheduledTask(id, runnable, delayTicks, 0);
        tasks.put(id, task);
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
        int id = taskIdCounter.getAndIncrement();
        ScheduledTask task = new ScheduledTask(id, runnable, delayTicks, periodTicks);
        tasks.put(id, task);
        return task;
    }
    
    /**
     * Cancels a scheduled task by ID.
     */
    public static void cancelTask(int taskId) {
        // ⚡ Bolt: O(1) cancellation lookup without iterating through entire task list
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
