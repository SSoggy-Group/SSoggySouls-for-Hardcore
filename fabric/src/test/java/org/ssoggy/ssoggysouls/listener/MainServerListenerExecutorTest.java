package org.ssoggy.ssoggysouls.listener;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainServerListenerExecutorTest {

    @Test
    void testDbExecutorExecutesOnNamedDaemonThread() throws ExecutionException, InterruptedException, TimeoutException {
        assertNotNull(MainServerListener.DB_EXECUTOR, "DB_EXECUTOR should be initialized");

        CompletableFuture<String> future = new CompletableFuture<>();
        MainServerListener.DB_EXECUTOR.execute(() -> {
            future.complete(Thread.currentThread().getName());
        });

        String threadName = future.get(5, TimeUnit.SECONDS);
        assertTrue(threadName.startsWith("ssoggysouls-db-"), "Thread name should start with 'ssoggysouls-db-', got: " + threadName);
    }
}
