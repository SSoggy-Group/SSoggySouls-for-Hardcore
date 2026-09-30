package org.ssoggy.ssoggysouls.util;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateCheckerTest {

    private static final String CURRENT_VERSION = "1.0.0";
    private static final String LATEST_VERSION_WITH_V = "v1.1.0";
    private static final String SAME_VERSION = "1.0.0";
    private static final String OLDER_VERSION = "0.9.0";
    private static final String LATEST_MSG = "You are running the latest version!";
    private static final String FAILED_CHECK_MSG = "Failed to check for updates";
    private static final String FAILED_PARSE_MSG = "Failed to parse update response";
    private static final String MISSING_TAG_MSG = "Update response missing 'tag_name'";

    private Plugin plugin;
    private PluginMeta pluginMeta;
    private Server server;
    private BukkitScheduler scheduler;
    private Logger logger;
    private HttpClient httpClient;
    @SuppressWarnings("unchecked")
    private HttpResponse<String> httpResponse = mock(HttpResponse.class);

    @BeforeEach
    void setUp() {
        plugin = mock(Plugin.class);
        pluginMeta = mock(PluginMeta.class);
        server = mock(Server.class);
        scheduler = mock(BukkitScheduler.class);
        logger = mock(Logger.class);
        httpClient = mock(HttpClient.class);

        when(plugin.getPluginMeta()).thenReturn(pluginMeta);
        when(pluginMeta.getVersion()).thenReturn(CURRENT_VERSION);
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
    }

    @Test
    void testCheckForUpdatesNewerVersionAvailable() {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"tag_name\":\"" + LATEST_VERSION_WITH_V + "\"}");
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        updateChecker.checkForUpdates();

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTask(eq(plugin), runnableCaptor.capture());

        Runnable capturedTask = runnableCaptor.getValue();
        capturedTask.run();

        verify(logger).info("║              ⚡ UPDATE AVAILABLE ⚡                           ║");
    }

    @Test
    void testCheckForUpdatesSameVersion() {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"tag_name\":\"" + SAME_VERSION + "\"}");
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        updateChecker.checkForUpdates();

        verify(logger).info(LATEST_MSG);
        verify(scheduler, never()).runTask(any(Plugin.class), any(Runnable.class));
    }

    @Test
    void testCheckForUpdatesOlderVersion() {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"tag_name\":\"" + OLDER_VERSION + "\"}");
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        updateChecker.checkForUpdates();

        verify(logger).info(LATEST_MSG);
        verify(scheduler, never()).runTask(any(Plugin.class), any(Runnable.class));
    }

    @Test
    void testCheckForUpdatesNon200Response() {
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        updateChecker.checkForUpdates();

        verify(logger).log(eq(Level.WARNING), eq("Failed to check for updates. HTTP response code: {0}"), eq(404));
        verify(scheduler, never()).runTask(any(Plugin.class), any(Runnable.class));
    }

    @Test
    void testCheckForUpdatesThrowable() {
        IOException exception = new IOException("Connection failed");
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        updateChecker.checkForUpdates();

        verify(logger).log(eq(Level.WARNING), eq(FAILED_CHECK_MSG), eq(exception));
        verify(scheduler, never()).runTask(any(Plugin.class), any(Runnable.class));
    }

    @Test
    void testCheckForUpdatesInvalidJson() {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("not a json string");
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        updateChecker.checkForUpdates();

        verify(logger).log(eq(Level.WARNING), eq(FAILED_PARSE_MSG), any(Exception.class));
        verify(scheduler, never()).runTask(any(Plugin.class), any(Runnable.class));
    }

    @Test
    void testCheckForUpdatesMissingTagName() {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{}");
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(httpResponse));

        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        updateChecker.checkForUpdates();

        verify(logger).warning(MISSING_TAG_MSG);
        verify(scheduler, never()).runTask(any(Plugin.class), any(Runnable.class));
    }
}
