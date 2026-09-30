package org.ssoggy.ssoggysouls.util;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateCheckerTest {

    private Plugin plugin;
    private PluginMeta pluginMeta;
    private Logger logger;
    private Server server;
    private BukkitScheduler scheduler;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        plugin = mock(Plugin.class);
        pluginMeta = mock(PluginMeta.class);
        logger = mock(Logger.class);
        server = mock(Server.class);
        scheduler = mock(BukkitScheduler.class);
        httpClient = mock(HttpClient.class);

        when(plugin.getPluginMeta()).thenReturn(pluginMeta);
        when(pluginMeta.getVersion()).thenReturn("1.0.0");
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
    }

    @Test
    void testParseVersionPart() {
        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);

        assertEquals(1, updateChecker.parseVersionPart("1"));
        assertEquals(10, updateChecker.parseVersionPart("10"));
        assertEquals(2, updateChecker.parseVersionPart("v2"));
        assertEquals(3, updateChecker.parseVersionPart("beta3"));
        assertEquals(0, updateChecker.parseVersionPart(""));
        assertEquals(0, updateChecker.parseVersionPart("abc"));
    }

    @ParameterizedTest
    @CsvSource({
        "1.0.1, 1.0.0, true",
        "1.1.0, 1.0.0, true",
        "2.0.0, 1.0.0, true",
        "1.0.0, 1.0.0, false",
        "1.0.0, 1.0.1, false",
        "1.0.0, 1.1.0, false",
        "1.0.0, 2.0.0, false",
        "1.0.0.1, 1.0.0, true",
        "1.0.0, 1.0.0.1, false"
    })
    void testIsNewerVersion(String latest, String current, boolean expected) {
        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        assertEquals(expected, updateChecker.isNewerVersion(latest, current));
    }

    @Test
    void testCheckForUpdates_ThrowableError() {
        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        Throwable exception = new RuntimeException("Network error");
        CompletableFuture<HttpResponse<String>> future = CompletableFuture.failedFuture(exception);

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((CompletableFuture) future);

        updateChecker.checkForUpdates();

        verify(logger).log(eq(Level.WARNING), eq("Failed to check for updates"), any(Throwable.class));
    }

    @Test
    void testCheckForUpdates_Non200Status() {
        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);
        CompletableFuture<HttpResponse<String>> future = CompletableFuture.completedFuture(response);

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((CompletableFuture) future);

        updateChecker.checkForUpdates();

        verify(logger).log(eq(Level.WARNING), eq("Failed to check for updates. HTTP response code: {0}"), eq(404));
    }

    @Test
    void testCheckForUpdates_InvalidJson() {
        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("invalid json");
        CompletableFuture<HttpResponse<String>> future = CompletableFuture.completedFuture(response);

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((CompletableFuture) future);

        updateChecker.checkForUpdates();

        verify(logger).log(eq(Level.WARNING), eq("Failed to parse update response"), any(Exception.class));
    }

    @Test
    void testCheckForUpdates_LatestVersionSameOrOlder() {
        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"tag_name\":\"v1.0.0\"}");
        CompletableFuture<HttpResponse<String>> future = CompletableFuture.completedFuture(response);

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((CompletableFuture) future);

        updateChecker.checkForUpdates();

        verify(logger).info("You are running the latest version!");
    }

    @Test
    void testCheckForUpdates_NewerVersionAvailable() {
        UpdateChecker updateChecker = new UpdateChecker(plugin, httpClient);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"tag_name\":\"v1.1.0\"}");
        CompletableFuture<HttpResponse<String>> future = CompletableFuture.completedFuture(response);

        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn((CompletableFuture) future);

        when(scheduler.runTask(eq(plugin), any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(1);
            runnable.run();
            return null;
        });

        updateChecker.checkForUpdates();

        verify(logger).info("║              ⚡ UPDATE AVAILABLE ⚡                           ║");
    }
}
