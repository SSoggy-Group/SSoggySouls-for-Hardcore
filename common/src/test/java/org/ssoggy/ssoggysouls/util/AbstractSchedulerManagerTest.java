package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AbstractSchedulerManagerTest {

    private static class TestSchedulerManager extends AbstractSchedulerManager {
        public static void testTick() {
            tickTasks(null);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Field tasksField = AbstractSchedulerManager.class.getDeclaredField("tasks");
        tasksField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, ScheduledTask> tasks = (Map<Integer, ScheduledTask>) tasksField.get(null);
        tasks.clear();
    }

    @Test
    void testRunLater_addsTask() {
        Runnable mockRunnable = () -> {};
        ScheduledTask task = AbstractSchedulerManager.runLater(mockRunnable, 10);

        assertNotNull(task);
        assertFalse(task.isCancelled());
    }

    @Test
    void testRunTimer_addsTask() {
        Runnable mockRunnable = () -> {};
        ScheduledTask task = AbstractSchedulerManager.runTimer(mockRunnable, 10, 5);

        assertNotNull(task);
        assertFalse(task.isCancelled());
    }

    @Test
    void testCancelTask_cancelsAndRemoves() {
        Runnable mockRunnable = () -> {};
        ScheduledTask task = AbstractSchedulerManager.runLater(mockRunnable, 10);

        AbstractSchedulerManager.cancelTask(task.getTaskId());

        assertTrue(task.isCancelled());
        TestSchedulerManager.testTick();

        try {
            Field tasksField = AbstractSchedulerManager.class.getDeclaredField("tasks");
            tasksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, ScheduledTask> tasks = (Map<Integer, ScheduledTask>) tasksField.get(null);
            assertTrue(tasks.isEmpty());
        } catch (Exception e) {
            fail("Reflection failed", e);
        }
    }

    @Test
    void testTick_executesAndRemovesLaterTask() {
        boolean[] executed = {false};
        Runnable mockRunnable = () -> executed[0] = true;

        AbstractSchedulerManager.runLater(mockRunnable, 1);
        TestSchedulerManager.testTick();
        assertFalse(executed[0]);

        TestSchedulerManager.testTick();
        assertTrue(executed[0]);

        try {
            Field tasksField = AbstractSchedulerManager.class.getDeclaredField("tasks");
            tasksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Integer, ScheduledTask> tasks = (Map<Integer, ScheduledTask>) tasksField.get(null);
            assertTrue(tasks.isEmpty());
        } catch (Exception e) {
            fail("Reflection failed", e);
        }
    }

    @Test
    void testTick_executesAndReschedulesTimerTask() {
        int[] executionCount = {0};
        Runnable mockRunnable = () -> executionCount[0]++;

        AbstractSchedulerManager.runTimer(mockRunnable, 0, 2);

        TestSchedulerManager.testTick();
        assertEquals(1, executionCount[0]);

        TestSchedulerManager.testTick();
        assertEquals(1, executionCount[0]);

        TestSchedulerManager.testTick();
        assertEquals(2, executionCount[0]);
    }
}
