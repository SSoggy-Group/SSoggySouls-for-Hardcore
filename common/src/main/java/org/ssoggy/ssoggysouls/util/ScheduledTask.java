package org.ssoggy.ssoggysouls.util;

public class ScheduledTask {
    private final int taskId;
    private final Runnable runnable;
    int delayTicks; // package-private for scheduler access
    final int periodTicks; // package-private for scheduler access
    boolean cancelled = false; // package-private for scheduler access

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

    public Runnable getRunnable() {
        return runnable;
    }
}
