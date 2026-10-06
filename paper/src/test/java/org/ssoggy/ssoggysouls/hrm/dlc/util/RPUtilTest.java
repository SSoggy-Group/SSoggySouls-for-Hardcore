package org.ssoggy.ssoggysouls.hrm.dlc.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class RPUtilTest {

    private File tempFolder;
    private MockedStatic<Bukkit> bukkitMock;

    @BeforeEach
    void setup() throws IOException {
        tempFolder = Files.createTempDirectory("rputil").toFile();
        JavaPlugin mockPlugin = mock(JavaPlugin.class);
        Server mockServer = mock(Server.class);
        when(mockPlugin.getServer()).thenReturn(mockServer);
        when(mockPlugin.getDataFolder()).thenReturn(tempFolder);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));

        RPStatic.init(mockPlugin);

        bukkitMock = Mockito.mockStatic(Bukkit.class);
    }

    @AfterEach
    void tearDown() {
        if (bukkitMock != null) bukkitMock.close();
        if (RPStatic.USERNAME_CACHE != null) RPStatic.USERNAME_CACHE.shutdown();
        if (RPStatic.STATS_STORAGE != null) RPStatic.STATS_STORAGE.shutdown();
        if (RPStatic.DEAD_STORAGE != null) RPStatic.DEAD_STORAGE.shutdown();
        if (RPStatic.SOCIAL_STORAGE != null) RPStatic.SOCIAL_STORAGE.shutdown();

        File[] files = tempFolder.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        tempFolder.delete();
    }

    @Test
    void testAddUsernameToCacheSuccess() {
        UUID uuid = UUID.randomUUID();
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getName()).thenReturn("TestUser");
        bukkitMock.when(() -> Bukkit.getOfflinePlayer(uuid)).thenReturn(offlinePlayer);

        String username = RPUtil.addUsernameToCache(uuid);
        assertEquals("TestUser", username);
        assertEquals("TestUser", RPStatic.USERNAME_CACHE.getValue("usernamecache", uuid.toString()));
    }

    @Test
    void testAddUsernameToCacheExceptionHandled() {
        UUID uuid = UUID.randomUUID();
        bukkitMock.when(() -> Bukkit.getOfflinePlayer(uuid)).thenThrow(new RuntimeException("Lookup failed"));

        String username = RPUtil.addUsernameToCache(uuid);
        assertNull(username);
    }

    @Test
    void testGetUsernameFromCacheHit() {
        UUID uuid = UUID.randomUUID();
        RPStatic.USERNAME_CACHE.setValue("usernamecache", uuid.toString(), "CachedUser");

        String username = RPUtil.getUsernameFromCache(uuid);
        assertEquals("CachedUser", username);
        bukkitMock.verify(() -> Bukkit.getOfflinePlayer(uuid), never());
    }

    @Test
    void testGetUsernameFromCacheMiss() {
        UUID uuid = UUID.randomUUID();
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getName()).thenReturn("FetchedUser");
        bukkitMock.when(() -> Bukkit.getOfflinePlayer(uuid)).thenReturn(offlinePlayer);

        String username = RPUtil.getUsernameFromCache(uuid);
        assertEquals("FetchedUser", username);
    }

    @Test
    void testGetAllUsernamesFromCacheWithFilter() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        RPStatic.USERNAME_CACHE.setValue("usernamecache", u1.toString(), "Alice");
        RPStatic.USERNAME_CACHE.setValue("usernamecache", u2.toString(), "Bob");
        RPStatic.USERNAME_CACHE.setValue("usernamecache", "invalid-uuid", "Invalid");

        Map<UUID, String> results = RPUtil.getAllUsernamesFromCache((uuid, name) -> name.startsWith("A"));
        assertEquals(1, results.size());
        assertEquals("Alice", results.get(u1));
    }

    @Test
    void testGetAllUsernamesFromCacheNoFilter() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        RPStatic.USERNAME_CACHE.setValue("usernamecache", u1.toString(), "Alice");
        RPStatic.USERNAME_CACHE.setValue("usernamecache", u2.toString(), "Bob");

        Map<UUID, String> results = RPUtil.getAllUsernamesFromCache(null);
        assertEquals(2, results.size());
        assertEquals("Alice", results.get(u1));
        assertEquals("Bob", results.get(u2));
    }

    @Test
    void testGetAllUsernamesFromCacheEmptySection() {
        Map<UUID, String> results = RPUtil.getAllUsernamesFromCache(null);
        assertTrue(results.isEmpty());
    }
}
