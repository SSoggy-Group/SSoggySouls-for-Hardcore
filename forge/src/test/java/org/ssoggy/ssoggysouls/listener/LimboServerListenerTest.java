package org.ssoggy.ssoggysouls.listener;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ssoggy.ssoggysouls.database.DatabaseManager;
import org.ssoggy.ssoggysouls.database.PlayerData;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LimboServerListenerTest {

    private DatabaseManager db;

    @BeforeEach
    void setUp() {
        db = mock(DatabaseManager.class);
        LimboServerListener.setDatabase(db);
    }

    @Test
    void testOnPlayerJoinDbNullDoesNothing() {
        LimboServerListener.setDatabase(null);
        PlayerEvent.PlayerLoggedInEvent event = mock(PlayerEvent.PlayerLoggedInEvent.class);
        ServerPlayer player = mock(ServerPlayer.class);
        when(event.getEntity()).thenReturn(player);

        LimboServerListener.onPlayerJoin(event);

        verifyNoInteractions(player);
    }

    @Test
    void testOnPlayerJoinDeadPlayer() throws Exception {
        UUID uuid = UUID.randomUUID();
        ServerPlayer player = mock(ServerPlayer.class);
        Level level = mock(Level.class);
        MinecraftServer server = mock(MinecraftServer.class);
        PlayerEvent.PlayerLoggedInEvent event = mock(PlayerEvent.PlayerLoggedInEvent.class);

        when(event.getEntity()).thenReturn(player);
        when(player.getUUID()).thenReturn(uuid);
        when(player.level()).thenReturn(level);
        when(level.getServer()).thenReturn(server);

        PlayerData data = mock(PlayerData.class);
        when(data.isDead()).thenReturn(true);
        when(db.getPlayerStrict(uuid)).thenReturn(data);

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            latch.countDown();
            return null;
        }).when(server).execute(any(Runnable.class));

        LimboServerListener.onPlayerJoin(event);

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Server execute task was not executed in time");
        verify(db).getPlayerStrict(uuid);
    }

    @Test
    void testOnPlayerJoinVisitorPlayer() throws Exception {
        UUID uuid = UUID.randomUUID();
        ServerPlayer player = mock(ServerPlayer.class);
        Level level = mock(Level.class);
        MinecraftServer server = mock(MinecraftServer.class);
        PlayerEvent.PlayerLoggedInEvent event = mock(PlayerEvent.PlayerLoggedInEvent.class);

        when(event.getEntity()).thenReturn(player);
        when(player.getUUID()).thenReturn(uuid);
        when(player.level()).thenReturn(level);
        when(level.getServer()).thenReturn(server);

        PlayerData data = mock(PlayerData.class);
        when(data.isDead()).thenReturn(false);
        when(db.getPlayerStrict(uuid)).thenReturn(data);

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            latch.countDown();
            return null;
        }).when(server).execute(any(Runnable.class));

        LimboServerListener.onPlayerJoin(event);

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Server execute task was not executed in time");
        verify(player).setGameMode(GameType.SURVIVAL);
    }

    @Test
    void testOnPlayerJoinSqlExceptionTreatsAsDead() throws Exception {
        UUID uuid = UUID.randomUUID();
        ServerPlayer player = mock(ServerPlayer.class);
        Level level = mock(Level.class);
        MinecraftServer server = mock(MinecraftServer.class);
        PlayerEvent.PlayerLoggedInEvent event = mock(PlayerEvent.PlayerLoggedInEvent.class);

        when(event.getEntity()).thenReturn(player);
        when(player.getUUID()).thenReturn(uuid);
        when(player.getScoreboardName()).thenReturn("TestPlayer");
        when(player.level()).thenReturn(level);
        when(level.getServer()).thenReturn(server);

        when(db.getPlayerStrict(uuid)).thenThrow(new SQLException("Database connection failed"));

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            latch.countDown();
            return null;
        }).when(server).execute(any(Runnable.class));

        LimboServerListener.onPlayerJoin(event);

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Server execute task was not executed in time");
        verify(db).getPlayerStrict(uuid);
    }
}
