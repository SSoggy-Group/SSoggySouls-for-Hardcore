package org.ssoggy.ssoggysouls.listener;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerLifecycleListenerTest {

    @Test
    void testDbExecutorIsDedicatedAndNotCommonPool() throws ExecutionException, InterruptedException {
        CompletableFuture<String> threadNameFuture = CompletableFuture.supplyAsync(
                () -> Thread.currentThread().getName(),
                ServerLifecycleListener.DB_EXECUTOR
        );
        CompletableFuture<Boolean> isDaemonFuture = CompletableFuture.supplyAsync(
                () -> Thread.currentThread().isDaemon(),
                ServerLifecycleListener.DB_EXECUTOR
        );

        String threadName = threadNameFuture.get();
        boolean isDaemon = isDaemonFuture.get();

        assertTrue(threadName.startsWith("SSoggySouls-DB-IO-"), "Thread should be named SSoggySouls-DB-IO- but was " + threadName);
        assertTrue(isDaemon, "DB executor thread should be a daemon thread");
        assertFalse(threadName.contains("ForkJoinPool"), "DB task should not run on CommonPool");
    }
}
