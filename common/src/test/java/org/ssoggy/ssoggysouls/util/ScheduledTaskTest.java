package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScheduledTaskTest {

    @Test
    void testConstructorAndGetters() {
        boolean[] executed = {false};
        Runnable runnable = () -> executed[0] = true;
        ScheduledTask task = new ScheduledTask(101, runnable, 5, 20);

        assertEquals(101, task.getTaskId());
        assertSame(runnable, task.getRunnable());
        assertFalse(task.isCancelled());
        assertFalse(task.cancelled);
        assertEquals(5, task.delayTicks);
        assertEquals(20, task.periodTicks);

        task.getRunnable().run();
        assertTrue(executed[0]);
    }

    @Test
    void testCancel() {
        Runnable runnable = () -> {};
        ScheduledTask task = new ScheduledTask(1, runnable, 0, 0);

        assertFalse(task.isCancelled());
        assertFalse(task.cancelled);

        task.cancel();

        assertTrue(task.isCancelled());
        assertTrue(task.cancelled);
    }

    @Test
    void testPackagePrivateFieldAccess() {
        Runnable runnable = () -> {};
        ScheduledTask task = new ScheduledTask(42, runnable, 10, 15);

        task.delayTicks = 8;
        assertEquals(8, task.delayTicks);

        task.cancelled = true;
        assertTrue(task.isCancelled());
    }
}
