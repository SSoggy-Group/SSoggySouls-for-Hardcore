package org.ssoggy.ssoggysouls.hrm.dlc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhostState extends SavedData {

    private static final String DEATH_LOCATIONS = "deathLocations";
    private static final String DEATH_HOLDERS = "deathHolders";

    public final Map<UUID, BlockPos> deathLocations = new HashMap<>();
    public final Map<UUID, UUID> deathHolders = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag locations = new CompoundTag();
        deathLocations.forEach((uuid, pos) -> locations.putLong(uuid.toString(), pos.asLong()));
        tag.put(DEATH_LOCATIONS, locations);

        CompoundTag holders = new CompoundTag();
        deathHolders.forEach((ghostId, holderId) -> holders.putUUID(ghostId.toString(), holderId));
        tag.put(DEATH_HOLDERS, holders);

        return tag;
    }

    public static GhostState load(CompoundTag tag, HolderLookup.Provider registries) {
        GhostState state = new GhostState();

        if (tag.contains(DEATH_LOCATIONS)) {
            CompoundTag locations = tag.getCompound(DEATH_LOCATIONS);
            for (String key : locations.getAllKeys()) {
                state.deathLocations.put(UUID.fromString(key), BlockPos.of(locations.getLong(key)));
            }
        }

        if (tag.contains(DEATH_HOLDERS)) {
            CompoundTag holders = tag.getCompound(DEATH_HOLDERS);
            for (String key : holders.getAllKeys()) {
                state.deathHolders.put(UUID.fromString(key), holders.getUUID(key));
            }
        }

        return state;
    }

    public static GhostState getServerState(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        GhostState::new,
                        GhostState::load,
                        null
                ),
                "ssoggysouls_ghost_data"
        );
    }
}
