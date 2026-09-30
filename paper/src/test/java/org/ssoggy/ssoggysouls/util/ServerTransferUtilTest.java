package org.ssoggy.ssoggysouls.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ssoggy.ssoggysouls.SSoggySouls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServerTransferUtilTest {

    @Mock
    private SSoggySouls plugin;

    @Mock
    private Player player;

    private MockedStatic<SSoggySouls> staticPluginMock;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        staticPluginMock = Mockito.mockStatic(SSoggySouls.class);
        staticPluginMock.when(SSoggySouls::getInstance).thenReturn(plugin);

        when(player.getName()).thenReturn("TestPlayer");
        when(plugin.getLimboServerName()).thenReturn("limbo");
        when(plugin.getMainServerName()).thenReturn("main");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (staticPluginMock != null) {
            staticPluginMock.close();
        }
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void testSendToServer() throws IOException {
        String targetServer = "survival";

        ServerTransferUtil.sendToServer(player, targetServer);

        verify(plugin).debug("Sending TestPlayer to server: " + targetServer);

        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(player).sendPluginMessage(eq(plugin), eq("BungeeCord"), payloadCaptor.capture());

        byte[] payload = payloadCaptor.getValue();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));

        String subChannel = in.readUTF();
        String serverArg = in.readUTF();

        assertEquals("Connect", subChannel);
        assertEquals(targetServer, serverArg);
    }

    @Test
    void testSendToLimbo() throws IOException {
        ServerTransferUtil.sendToLimbo(player);

        verify(plugin).debug("Sending TestPlayer to server: limbo");

        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(player).sendPluginMessage(eq(plugin), eq("BungeeCord"), payloadCaptor.capture());

        byte[] payload = payloadCaptor.getValue();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));

        assertEquals("Connect", in.readUTF());
        assertEquals("limbo", in.readUTF());
    }

    @Test
    void testSendToMain() throws IOException {
        ServerTransferUtil.sendToMain(player);

        verify(plugin).debug("Sending TestPlayer to server: main");

        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(player).sendPluginMessage(eq(plugin), eq("BungeeCord"), payloadCaptor.capture());

        byte[] payload = payloadCaptor.getValue();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));

        assertEquals("Connect", in.readUTF());
        assertEquals("main", in.readUTF());
    }
}
