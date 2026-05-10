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

    private static final String DEATH_LOCATIONS = "deathLocations";
    private static final String DEATH_HOLDERS = "deathHolders";

    public final Map<UUID, BlockPos> deathLocations = new HashMap<>();
    public final Map<UUID, UUID> deathHolders = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound locations = new NbtCompound();
        deathLocations.forEach((uuid, pos) -> locations.putLong(uuid.toString(), pos.asLong()));
        nbt.put(DEATH_LOCATIONS, locations);

        NbtCompound holders = new NbtCompound();
        deathHolders.forEach((ghostId, holderId) -> holders.putUuid(ghostId.toString(), holderId));
        nbt.put(DEATH_HOLDERS, holders);

        return nbt;
    }

    public static GhostState fromNbt(NbtCompound nbt) {
        GhostState state = new GhostState();

        if (nbt.contains(DEATH_LOCATIONS)) {
            NbtCompound locations = nbt.getCompound(DEATH_LOCATIONS);
            for (String key : locations.getKeys()) {
                state.deathLocations.put(UUID.fromString(key), BlockPos.fromLong(locations.getLong(key)));
            }
        }

        if (nbt.contains(DEATH_HOLDERS)) {
            NbtCompound holders = nbt.getCompound(DEATH_HOLDERS);
            for (String key : holders.getKeys()) {
                state.deathHolders.put(UUID.fromString(key), holders.getUuid(key));
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
