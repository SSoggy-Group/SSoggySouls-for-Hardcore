package org.ssoggy.ssoggysouls.util;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
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
    private Player player;

    @Mock
    private SSoggySouls plugin;

    private MockedStatic<SSoggySouls> pluginMockedStatic;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        pluginMockedStatic = Mockito.mockStatic(SSoggySouls.class);
        pluginMockedStatic.when(SSoggySouls::getInstance).thenReturn(plugin);
        when(player.getName()).thenReturn("TestPlayer");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (pluginMockedStatic != null) {
            pluginMockedStatic.close();
        }
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void testSendToServer() {
        String targetServer = "survival";

        ServerTransferUtil.sendToServer(player, targetServer);

        verify(plugin).debug("Sending TestPlayer to server: " + targetServer);

        ArgumentCaptor<byte[]> messageCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(player).sendPluginMessage(eq(plugin), eq("BungeeCord"), messageCaptor.capture());

        byte[] sentBytes = messageCaptor.getValue();
        ByteArrayDataInput in = ByteStreams.newDataInput(sentBytes);

        String subchannel = in.readUTF();
        String server = in.readUTF();

        assertEquals("Connect", subchannel);
        assertEquals(targetServer, server);
    }

    @Test
    void testSendToLimbo() {
        when(plugin.getLimboServerName()).thenReturn("limbo_server");

        ServerTransferUtil.sendToLimbo(player);

        ArgumentCaptor<byte[]> messageCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(player).sendPluginMessage(eq(plugin), eq("BungeeCord"), messageCaptor.capture());

        ByteArrayDataInput in = ByteStreams.newDataInput(messageCaptor.getValue());
        assertEquals("Connect", in.readUTF());
        assertEquals("limbo_server", in.readUTF());
    }

    @Test
    void testSendToMain() {
        when(plugin.getMainServerName()).thenReturn("main_server");

        ServerTransferUtil.sendToMain(player);

        ArgumentCaptor<byte[]> messageCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(player).sendPluginMessage(eq(plugin), eq("BungeeCord"), messageCaptor.capture());

        ByteArrayDataInput in = ByteStreams.newDataInput(messageCaptor.getValue());
        assertEquals("Connect", in.readUTF());
        assertEquals("main_server", in.readUTF());
    }
}
