package org.ssoggy.ssoggysouls.listener;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class LimboServerListenerTest {

    @Test
    @DisplayName("onServerTick invokes onLimboTick and rescues void-falling adventure players")
    void testOnServerTickRescuesVoidPlayer() {
        MinecraftServer server = mock(MinecraftServer.class);
        PlayerList playerList = mock(PlayerList.class);
        ServerPlayer player = mock(ServerPlayer.class);

        when(server.getTickCount()).thenReturn(20);
        when(server.getPlayerList()).thenReturn(playerList);
        when(playerList.getPlayers()).thenReturn(List.of(player));

        player.gameMode = mock(net.minecraft.server.level.ServerPlayerGameMode.class);
        when(player.gameMode.getGameModeForPlayer()).thenReturn(GameType.ADVENTURE);
        net.minecraft.world.level.Level level = mock(net.minecraft.world.level.Level.class);
        when(level.getMinY()).thenReturn(0);
        when(player.level()).thenReturn(level);
        when(player.getY()).thenReturn(-10.0);

        net.minecraft.server.level.ServerLevel overworld = mock(net.minecraft.server.level.ServerLevel.class);
        when(server.overworld()).thenReturn(overworld);
        net.minecraft.world.level.storage.PrimaryLevelData respawnData = mock(net.minecraft.world.level.storage.PrimaryLevelData.class);
        when(server.getRespawnData()).thenReturn(respawnData);
        when(respawnData.pos()).thenReturn(net.minecraft.core.BlockPos.ZERO);

        TickEvent.ServerTickEvent.Post event = mock(TickEvent.ServerTickEvent.Post.class);
        when(event.server()).thenReturn(server);

        LimboServerListener.onServerTick(event);

        verify(server, times(1)).getTickCount();
        verify(player, times(1)).resetFallDistance();
    }

    @Test
    @DisplayName("onServerTick on non-void ticks does not check player list")
    void testOnServerTickNonVoidTick() {
        MinecraftServer server = mock(MinecraftServer.class);
        when(server.getTickCount()).thenReturn(5);

        TickEvent.ServerTickEvent.Post event = mock(TickEvent.ServerTickEvent.Post.class);
        when(event.server()).thenReturn(server);

        LimboServerListener.onServerTick(event);

        verify(server, times(1)).getTickCount();
        verify(server, never()).getPlayerList();
    }
}
