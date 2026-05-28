package org.ssoggy.ssoggysouls.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.network.Channel;
import org.ssoggy.ssoggysouls.SSoggySoulsMod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class ServerTransferUtil {

    private static final int PROTOCOL_VERSION = 1;
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(SSoggySoulsMod.MODID, "bungee_connect"))
            .networkProtocolVersion(PROTOCOL_VERSION)
            .acceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .simpleChannel();

    public static void register() {
        CHANNEL.messageBuilder(BungeeConnectPayload.class, 1)
                .codec(BungeeConnectPayload.CODEC)
                .consumerMainThread((payload, context) -> {}) // No action needed on the client or server side to handle this specific custom payload besides what the proxy does
                .add();
    }

    public static void sendToServer(ServerPlayer player, String serverName) {
        player.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(
                new BungeeConnectPayload(serverName)
        ));
    }

    public static void sendToLimbo(ServerPlayer player) {
        sendToServer(player, ConfigManager.getConfig().getLimboServerName());
    }

    public static void sendToMain(ServerPlayer player) {
        sendToServer(player, ConfigManager.getConfig().getMainServerName());
    }

    public record BungeeConnectPayload(String serverName) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BungeeConnectPayload> PAYLOAD_TYPE = new CustomPacketPayload.Type<>(
                ResourceLocation.fromNamespaceAndPath("bungeecord", "main")
        );

        public static final StreamCodec<FriendlyByteBuf, BungeeConnectPayload> CODEC = StreamCodec.of(
                (buf, value) -> {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(baos);
                        dos.writeUTF("Connect");
                        dos.writeUTF(value.serverName());
                        buf.writeBytes(baos.toByteArray());
                    } catch (IOException e) {
                        throw new UncheckedIOException("Failed to encode BungeeCord Connect payload", e);
                    }
                },
                buf -> {
                    try {
                        int length = buf.readableBytes();
                        if (length > 1024) {
                            throw new IOException("Payload too large: " + length);
                        }
                        byte[] bytes = new byte[length];
                        buf.readBytes(bytes);
                        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
                        dis.readUTF(); // Skip sub-channel name ("Connect")
                        return new BungeeConnectPayload(dis.readUTF());
                    } catch (IOException e) {
                        throw new UncheckedIOException("Failed to decode BungeeCord Connect payload", e);
                    }
                }
        );

        @Override
        public CustomPacketPayload.Type<BungeeConnectPayload> type() {
            return PAYLOAD_TYPE;
        }
    }
}
