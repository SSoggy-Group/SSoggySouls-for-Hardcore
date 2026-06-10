package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class AbstractSchedulerManagerTest {

    private static class TestSchedulerManager extends AbstractSchedulerManager {
        public static void testTick(Consumer<Exception> handler) {
            tickTasks(handler);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        Field tasksField = AbstractSchedulerManager.class.getDeclaredField("tasks");
        tasksField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, ScheduledTask> tasks = (Map<Integer, ScheduledTask>) tasksField.get(null);
        tasks.clear();

        Field counterField = AbstractSchedulerManager.class.getDeclaredField("taskIdCounter");
        counterField.setAccessible(true);
        AtomicInteger counter = (AtomicInteger) counterField.get(null);
        counter.set(0);
    }

    @Test
    void testRunLater_addsTask() {
        Runnable mockRunnable = () -> {};
        ScheduledTask task = AbstractSchedulerManager.runLater(mockRunnable, 10);

        assertNotNull(task);
        assertFalse(task.isCancelled());
        assertEquals(0, task.getTaskId());
    }

    @Test
    void testRunTimer_addsTask() {
        Runnable mockRunnable = () -> {};
        ScheduledTask task = AbstractSchedulerManager.runTimer(mockRunnable, 10, 5);

        assertNotNull(task);
        assertFalse(task.isCancelled());
        assertEquals(0, task.getTaskId());
    }

    @Test
    void testCancelTask_cancelsAndRemoves() {
        Runnable mockRunnable = () -> {};
        ScheduledTask task = AbstractSchedulerManager.runLater(mockRunnable, 10);

        AbstractSchedulerManager.cancelTask(task.getTaskId());

        assertTrue(task.isCancelled());
        TestSchedulerManager.testTick(null);

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
        TestSchedulerManager.testTick(null);
        assertFalse(executed[0]);

        TestSchedulerManager.testTick(null);
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

        TestSchedulerManager.testTick(null);
        assertEquals(1, executionCount[0]);

        TestSchedulerManager.testTick(null);
        assertEquals(1, executionCount[0]);

        TestSchedulerManager.testTick(null);
        assertEquals(2, executionCount[0]);
    }

    @Test
    void testTick_handlesExceptionsWithConsumer() {
        boolean[] errorHandled = {false};
        Runnable throwingRunnable = () -> {
            throw new RuntimeException("Test Exception");
        };

        AbstractSchedulerManager.runLater(throwingRunnable, 0);

        TestSchedulerManager.testTick(e -> {
            errorHandled[0] = true;
            assertEquals("Test Exception", e.getMessage());
        });

        assertTrue(errorHandled[0]);
    }

    @Test
    void testTick_handlesExceptionsWhenConsumerIsNull() {
        Runnable throwingRunnable = () -> {
            throw new RuntimeException("Test Exception");
        };

        AbstractSchedulerManager.runLater(throwingRunnable, 0);

        // Should not throw an exception out of tickTasks
        assertDoesNotThrow(() -> TestSchedulerManager.testTick(null));
    }

    @Test
    void testCancelTask_invalidIdDoesNothing() {
        // Just checking it doesn't crash when passing an invalid ID
        assertDoesNotThrow(() -> AbstractSchedulerManager.cancelTask(999));
    }
}
