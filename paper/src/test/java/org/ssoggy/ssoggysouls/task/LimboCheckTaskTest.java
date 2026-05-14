package org.ssoggy.ssoggysouls.task;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.ssoggy.ssoggysouls.SSoggySouls;
import org.ssoggy.ssoggysouls.database.DatabaseManager;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LimboCheckTaskTest {

    private MockedStatic<Bukkit> mockedBukkit;
    private SSoggySouls plugin;
    private DatabaseManager db;
    private BukkitScheduler scheduler;
    private Logger logger;

    @BeforeEach
    void setUp() {
        mockedBukkit = mockStatic(Bukkit.class);
        plugin = mock(SSoggySouls.class);
        db = mock(DatabaseManager.class);
        scheduler = mock(BukkitScheduler.class);
        logger = mock(Logger.class);

        when(plugin.getDatabaseManager()).thenReturn(db);
        when(plugin.getLogger()).thenReturn(logger);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
    }

    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Test
    void testAddRemovePlayer() {
        LimboCheckTask task = new LimboCheckTask(plugin);
        UUID uuid = UUID.randomUUID();

        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getUniqueId()).thenReturn(uuid);
        mockedBukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(mockPlayer);

        task.addPlayer(uuid);

        when(db.arePlayersDead(any())).thenReturn(Map.of(uuid, true));
        task.run();

        verify(db).arePlayersDead(any());

        task.removePlayer(uuid);
        task.run();

        verify(db, times(1)).arePlayersDead(any()); // No new calls
    }
}
