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
        PayloadTypeRegistry.playS2C().register(BungeeConnectPayload.ID, BungeeConnectPayload.CODEC);
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
        public static final CustomPayload.Id<BungeeConnectPayload> ID = new CustomPayload.Id<>(Identifier.of("bungeecord", "main"));

        public static final PacketCodec<PacketByteBuf, BungeeConnectPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString("Connect");
                buf.writeString(value.serverName());
            },
            buf -> {
                buf.readString(); // Skip sub-channel
                return new BungeeConnectPayload(buf.readString());
            }
        );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
}
