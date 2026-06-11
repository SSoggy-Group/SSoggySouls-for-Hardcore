package org.ssoggy.ssoggysouls.hrm.dlc.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GhostState extends PersistentState {

    private static final String DEATH_LOCATIONS = "deathLocations";
    private static final String DEATH_HOLDERS = "deathHolders";
    private static final String HEAD_BLOCK_LOCATIONS = "headBlockLocations";
    private static final String HEAD_LOCATION_DIMENSION = "dimension";
    private static final String HEAD_LOCATION_POS = "pos";

    public final Map<UUID, BlockPos> deathLocations = new HashMap<>();
    public final Map<UUID, UUID> deathHolders = new HashMap<>();
    private final Map<UUID, List<GlobalPos>> headBlockLocations = new HashMap<>();

    public void addHeadBlockLocation(UUID playerId, GlobalPos location) {
        headBlockLocations.computeIfAbsent(playerId, ignored -> new ArrayList<>()).add(location);
        markDirty();
    }

    public List<GlobalPos> consumeHeadBlockLocations(UUID playerId) {
        List<GlobalPos> removed = headBlockLocations.remove(playerId);
        if (removed != null) {
            markDirty();
        }
        return removed;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound locations = new NbtCompound();
        deathLocations.forEach((uuid, pos) -> locations.putLong(uuid.toString(), pos.asLong()));
        nbt.put(DEATH_LOCATIONS, locations);

        NbtCompound holders = new NbtCompound();
        deathHolders.forEach((ghostId, holderId) -> holders.putUuid(ghostId.toString(), holderId));
        nbt.put(DEATH_HOLDERS, holders);

        NbtCompound headLocations = new NbtCompound();
        headBlockLocations.forEach((playerId, playerLocations) -> {
            NbtList serializedLocations = new NbtList();
            for (GlobalPos location : playerLocations) {
                NbtCompound locationTag = new NbtCompound();
                locationTag.putString(HEAD_LOCATION_DIMENSION, location.dimension().getValue().toString());
                locationTag.putLong(HEAD_LOCATION_POS, location.pos().asLong());
                serializedLocations.add(locationTag);
            }
            headLocations.put(playerId.toString(), serializedLocations);
        });
        nbt.put(HEAD_BLOCK_LOCATIONS, headLocations);

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

        if (nbt.contains(HEAD_BLOCK_LOCATIONS)) {
            NbtCompound headLocations = nbt.getCompound(HEAD_BLOCK_LOCATIONS);
            for (String key : headLocations.getKeys()) {
                NbtList locations = headLocations.getList(key, NbtElement.COMPOUND_TYPE);
                List<GlobalPos> parsedLocations = new ArrayList<>();
                for (int i = 0; i < locations.size(); i++) {
                    NbtCompound locationTag = locations.getCompound(i);
                    Identifier dimensionId = Identifier.tryParse(locationTag.getString(HEAD_LOCATION_DIMENSION));
                    if (dimensionId == null) {
                        continue;
                    }
                    RegistryKey<World> dimension = RegistryKey.of(RegistryKeys.WORLD, dimensionId);
                    parsedLocations.add(GlobalPos.create(dimension, BlockPos.fromLong(locationTag.getLong(HEAD_LOCATION_POS))));
                }
                if (!parsedLocations.isEmpty()) {
                    state.headBlockLocations.put(UUID.fromString(key), parsedLocations);
                }
            }
        }

        return state;
    }

    public static GhostState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();

        Type<GhostState> type = new Type<>(
                GhostState::new,
                (nbt, ignoredRegistries) -> GhostState.fromNbt(nbt),
                null
        );

        return persistentStateManager.getOrCreate(type, "ssoggysouls_ghost_data");
    }
}
