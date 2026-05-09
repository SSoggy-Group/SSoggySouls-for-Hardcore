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

    private SSoggySouls pluginMock;
    private Player playerMock;
    private MockedStatic<SSoggySouls> staticPluginMock;

    @BeforeEach
    void setUp() {
        pluginMock = mock(SSoggySouls.class);
        playerMock = mock(Player.class);
        when(playerMock.getName()).thenReturn("TestPlayer");

        staticPluginMock = mockStatic(SSoggySouls.class);
        staticPluginMock.when(SSoggySouls::getInstance).thenReturn(pluginMock);
    }

    @AfterEach
    void tearDown() {
        if (staticPluginMock != null && !staticPluginMock.isClosed()) {
            staticPluginMock.close();
        }
    }

    @Test
    void testSendToServer() {
        String targetServer = "target_server_123";

        ServerTransferUtil.sendToServer(playerMock, targetServer);

        verify(pluginMock).debug("Sending TestPlayer to server: " + targetServer);

        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(playerMock).sendPluginMessage(eq(pluginMock), eq("BungeeCord"), bytesCaptor.capture());

        byte[] sentBytes = bytesCaptor.getValue();
        ByteArrayDataInput in = ByteStreams.newDataInput(sentBytes);

        assertEquals("Connect", in.readUTF());
        assertEquals(targetServer, in.readUTF());
    }

    @Test
    void testSendToLimbo() {
        String limboServer = "limbo_server";
        when(pluginMock.getLimboServerName()).thenReturn(limboServer);

        ServerTransferUtil.sendToLimbo(playerMock);

        verify(pluginMock).debug("Sending TestPlayer to server: " + limboServer);

        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(playerMock).sendPluginMessage(eq(pluginMock), eq("BungeeCord"), bytesCaptor.capture());

        byte[] sentBytes = bytesCaptor.getValue();
        ByteArrayDataInput in = ByteStreams.newDataInput(sentBytes);

        assertEquals("Connect", in.readUTF());
        assertEquals(limboServer, in.readUTF());
    }

    @Test
    void testSendToMain() {
        String mainServer = "main_server";
        when(pluginMock.getMainServerName()).thenReturn(mainServer);

        ServerTransferUtil.sendToMain(playerMock);

        verify(pluginMock).debug("Sending TestPlayer to server: " + mainServer);

        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(playerMock).sendPluginMessage(eq(pluginMock), eq("BungeeCord"), bytesCaptor.capture());

        byte[] sentBytes = bytesCaptor.getValue();
        ByteArrayDataInput in = ByteStreams.newDataInput(sentBytes);

        assertEquals("Connect", in.readUTF());
        assertEquals(mainServer, in.readUTF());
    }
}
