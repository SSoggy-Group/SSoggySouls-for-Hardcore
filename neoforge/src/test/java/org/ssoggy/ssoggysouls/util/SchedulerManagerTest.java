package org.ssoggy.ssoggysouls.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerManagerTest {

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
    void testOnServerTick_executesScheduledTasks() {
        boolean[] executed = {false};
        SchedulerManager.runLater(() -> executed[0] = true, 0);

        SchedulerManager.onServerTick(null);

        assertTrue(executed[0]);
    }

    @Test
    void testOnServerTick_handlesTaskExceptions() {
        boolean[] executedNext = {false};
        SchedulerManager.runLater(() -> {
            throw new RuntimeException("Test Exception");
        }, 0);
        SchedulerManager.runLater(() -> executedNext[0] = true, 0);

        assertDoesNotThrow(() -> SchedulerManager.onServerTick(null));
        assertTrue(executedNext[0]);
    }
}
