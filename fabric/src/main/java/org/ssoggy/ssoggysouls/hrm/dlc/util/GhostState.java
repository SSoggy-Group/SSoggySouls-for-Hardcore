package org.ssoggy.ssoggysouls.hrm.dlc.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhostState extends PersistentState {
    
    public final Map<UUID, BlockPos> deathLocations = new HashMap<>();
    public final Map<UUID, UUID> deathHolders = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound locationsNbt = new NbtCompound();
        deathLocations.forEach((uuid, pos) -> {
            locationsNbt.putLong(uuid.toString(), pos.asLong());
        });
        nbt.put("deathLocations", locationsNbt);

        NbtCompound holdersNbt = new NbtCompound();
        deathHolders.forEach((ghostId, holderId) -> {
            holdersNbt.putString(ghostId.toString(), holderId.toString());
        });
        nbt.put("deathHolders", holdersNbt);

        return nbt;
    }

    public static GhostState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        GhostState state = new GhostState();

        if (nbt.contains("deathLocations")) {
            NbtCompound locationsNbt = nbt.getCompound("deathLocations");
            for (String key : locationsNbt.getKeys()) {
                state.deathLocations.put(UUID.fromString(key), BlockPos.fromLong(locationsNbt.getLong(key)));
            }
        }

        if (nbt.contains("deathHolders")) {
            NbtCompound holdersNbt = nbt.getCompound("deathHolders");
            for (String key : holdersNbt.getKeys()) {
                state.deathHolders.put(UUID.fromString(key), UUID.fromString(holdersNbt.getString(key)));
            }
        }

        return state;
    }

    public static GhostState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();
        
        Type<GhostState> type = new Type<>(
                GhostState::new,
                GhostState::fromNbt,
                null
        );

        return persistentStateManager.getOrCreate(type, "ssoggysouls_ghost_data");
    }
}
