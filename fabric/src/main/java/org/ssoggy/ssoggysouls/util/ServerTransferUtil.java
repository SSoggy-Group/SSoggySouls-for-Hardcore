package org.ssoggy.ssoggysouls.util;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ServerTransferUtil {

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(BungeeConnectPayload.PAYLOAD_ID, BungeeConnectPayload.CODEC);
    }

    public static void sendToServer(ServerPlayerEntity player, String serverName) {
        ServerPlayNetworking.send(player, new BungeeConnectPayload(serverName));
    }

    public static void sendToLimbo(ServerPlayerEntity player) {
        sendToServer(player, ConfigManager.getConfig().getLimboServerName());
    }

    public static void sendToMain(ServerPlayerEntity player) {
        sendToServer(player, ConfigManager.getConfig().getMainServerName());
    }

    public record BungeeConnectPayload(String serverName) implements CustomPayload {
        public static final CustomPayload.Id<BungeeConnectPayload> PAYLOAD_ID = new CustomPayload.Id<>(
                Identifier.of("bungeecord", "main"));

            public static final PacketCodec<PacketByteBuf, BungeeConnectPayload> CODEC = PacketCodec.of(
                (value, buf) -> {
                    try {
                        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                        java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
                        dos.writeUTF("Connect");
                        dos.writeUTF(value.serverName());
                        buf.writeBytes(baos.toByteArray());
                    } catch (java.io.IOException e) {
                        throw new java.io.UncheckedIOException("Failed to encode BungeeCord Connect payload", e);
                    }
                },
                buf -> {
                    try {
                        byte[] bytes = new byte[buf.readableBytes()];
                        buf.readBytes(bytes);
                        java.io.DataInputStream dis = new java.io.DataInputStream(
                                new java.io.ByteArrayInputStream(bytes));
                        dis.readUTF(); // Skip sub-channel name ("Connect")
                        return new BungeeConnectPayload(dis.readUTF());
                    } catch (java.io.IOException e) {
                        throw new java.io.UncheckedIOException("Failed to decode BungeeCord Connect payload", e);
                    }
                });

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return PAYLOAD_ID;
        }
    }
}
