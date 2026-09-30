package org.ssoggy.ssoggysouls.util;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.ssoggy.ssoggysouls.SSoggySouls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServerTransferUtilTest {

    private MockedStatic<SSoggySouls> mockedPluginStatic;
    private SSoggySouls mockPlugin;
    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        mockedPluginStatic = mockStatic(SSoggySouls.class);
        mockPlugin = mock(SSoggySouls.class);
        mockPlayer = mock(Player.class);

        mockedPluginStatic.when(SSoggySouls::getInstance).thenReturn(mockPlugin);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        if (mockedPluginStatic != null) {
            mockedPluginStatic.close();
        }
    }

    @Test
    void sendToServer_sendsCorrectPluginMessage() {
        String targetServer = "survival-1";

        ServerTransferUtil.sendToServer(mockPlayer, targetServer);

        verify(mockPlugin).debug("Sending TestPlayer to server: " + targetServer);

        ArgumentCaptor<byte[]> messageCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(mockPlayer).sendPluginMessage(eq(mockPlugin), eq("BungeeCord"), messageCaptor.capture());

        byte[] sentBytes = messageCaptor.getValue();
        ByteArrayDataInput in = ByteStreams.newDataInput(sentBytes);

        assertEquals("Connect", in.readUTF());
        assertEquals(targetServer, in.readUTF());
    }

    @Test
    void sendToLimbo_fetchesLimboServerAndSends() {
        when(mockPlugin.getLimboServerName()).thenReturn("limbo-server");

        ServerTransferUtil.sendToLimbo(mockPlayer);

        verify(mockPlugin).getLimboServerName();

        ArgumentCaptor<byte[]> messageCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(mockPlayer).sendPluginMessage(eq(mockPlugin), eq("BungeeCord"), messageCaptor.capture());

        byte[] sentBytes = messageCaptor.getValue();
        ByteArrayDataInput in = ByteStreams.newDataInput(sentBytes);

        assertEquals("Connect", in.readUTF());
        assertEquals("limbo-server", in.readUTF());
    }

    @Test
    void sendToMain_fetchesMainServerAndSends() {
        when(mockPlugin.getMainServerName()).thenReturn("main-server");

        ServerTransferUtil.sendToMain(mockPlayer);

        verify(mockPlugin).getMainServerName();

        ArgumentCaptor<byte[]> messageCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(mockPlayer).sendPluginMessage(eq(mockPlugin), eq("BungeeCord"), messageCaptor.capture());

        byte[] sentBytes = messageCaptor.getValue();
        ByteArrayDataInput in = ByteStreams.newDataInput(sentBytes);

        assertEquals("Connect", in.readUTF());
        assertEquals("main-server", in.readUTF());
    }
}
