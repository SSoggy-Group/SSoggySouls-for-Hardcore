package org.ssoggy.ssoggysouls.hrm.dlc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GhostState extends SavedData {

    private static final String DEATH_LOCATIONS = "deathLocations";
    private static final String DEATH_DIMENSIONS = "deathDimensions";
    private static final String DEATH_HOLDERS = "deathHolders";
    private static final String HEAD_BLOCK_LOCATIONS = "headBlockLocations";
    private static final String HEAD_LOCATION_DIMENSION = "dimension";
    private static final String HEAD_LOCATION_POS = "pos";

    private final Map<UUID, BlockPos> deathLocations = new ConcurrentHashMap<>();
    // Dimension of each death location; absent for entries saved before it was tracked
    private final Map<UUID, ResourceKey<Level>> deathDimensions = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> deathHolders = new ConcurrentHashMap<>();
    private final Map<UUID, List<GlobalPos>> headBlockLocations = new HashMap<>();

    public BlockPos getDeathLocation(UUID ghostId) {
        return deathLocations.get(ghostId);
    }

    public void setDeathLocation(UUID ghostId, BlockPos pos) {
        deathLocations.put(ghostId, pos);
        deathDimensions.remove(ghostId);
        setDirty();
    }

    public void setDeathLocation(UUID ghostId, GlobalPos pos) {
        deathLocations.put(ghostId, pos.pos());
        deathDimensions.put(ghostId, pos.dimension());
        setDirty();
    }

    /** Dimension of the stored death location, or {@code null} if unknown (legacy data). */
    public ResourceKey<Level> getDeathDimension(UUID ghostId) {
        return deathDimensions.get(ghostId);
    }

    public void removeDeathLocation(UUID ghostId) {
        deathLocations.remove(ghostId);
        deathDimensions.remove(ghostId);
        setDirty();
    }

    /**
     * Returns a position a ghost can actually stand at. Deaths in the void would
     * otherwise pin the ghost (or its head) below the world forever: use the top of
     * the column, or the server spawn when the column is empty (e.g. End void).
     */
    public static GlobalPos reachableDeathPos(ServerLevel level, BlockPos pos) {
        if (pos.getY() >= level.getMinY()) {
            return GlobalPos.of(level.dimension(), pos);
        }
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        if (top > level.getMinY()) {
            return GlobalPos.of(level.dimension(), new BlockPos(pos.getX(), top, pos.getZ()));
        }
        return level.getServer().getRespawnData().globalPos();
    }

    public Map<UUID, BlockPos> getDeathLocations() {
        return Collections.unmodifiableMap(deathLocations);
    }

    public UUID getDeathHolder(UUID ghostId) {
        return deathHolders.get(ghostId);
    }

    public void setDeathHolder(UUID ghostId, UUID holderId) {
        deathHolders.put(ghostId, holderId);
        setDirty();
    }

    public void removeDeathHolder(UUID ghostId) {
        deathHolders.remove(ghostId);
        setDirty();
    }

    public Map<UUID, UUID> getDeathHolders() {
        return Collections.unmodifiableMap(deathHolders);
    }

    public void addHeadBlockLocation(UUID playerId, GlobalPos location) {
        headBlockLocations.computeIfAbsent(playerId, key -> new ArrayList<>()).add(location);
        setDirty();
    }

    public List<GlobalPos> consumeHeadBlockLocations(UUID playerId) {
        List<GlobalPos> removed = headBlockLocations.remove(playerId);
        if (removed != null) {
            setDirty();
        }
        return removed;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag locations = new CompoundTag();
        deathLocations.forEach((uuid, pos) -> locations.putLong(uuid.toString(), pos.asLong()));
        tag.put(DEATH_LOCATIONS, locations);

        CompoundTag dimensions = new CompoundTag();
        deathDimensions.forEach((uuid, dim) -> dimensions.putString(uuid.toString(), dim.identifier().toString()));
        tag.put(DEATH_DIMENSIONS, dimensions);

        CompoundTag holders = new CompoundTag();
        deathHolders.forEach((ghostId, holderId) -> holders.putString(ghostId.toString(), holderId.toString()));
        tag.put(DEATH_HOLDERS, holders);

        CompoundTag headLocations = new CompoundTag();
        headBlockLocations.forEach((playerId, playerLocations) -> {
            ListTag serializedLocations = new ListTag();
            for (GlobalPos location : playerLocations) {
                CompoundTag locationTag = new CompoundTag();
                locationTag.putString(HEAD_LOCATION_DIMENSION, location.dimension().identifier().toString());
                locationTag.putLong(HEAD_LOCATION_POS, location.pos().asLong());
                serializedLocations.add(locationTag);
            }
            headLocations.put(playerId.toString(), serializedLocations);
        });
        tag.put(HEAD_BLOCK_LOCATIONS, headLocations);

        return tag;
    }

    public static GhostState load(CompoundTag tag) {
        GhostState state = new GhostState();

        if (tag.contains(DEATH_LOCATIONS)) {
            CompoundTag locations = tag.getCompoundOrEmpty(DEATH_LOCATIONS);
            for (String key : locations.keySet()) {
                try {
                    state.deathLocations.put(UUID.fromString(key), BlockPos.of(locations.getLongOr(key, 0L)));
                } catch (IllegalArgumentException ignored) {
                    // Skip corrupt entry
                }
            }
        }

        if (tag.contains(DEATH_DIMENSIONS)) {
            CompoundTag dimensions = tag.getCompoundOrEmpty(DEATH_DIMENSIONS);
            for (String key : dimensions.keySet()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    Identifier dimensionId = Identifier.tryParse(dimensions.getStringOr(key, ""));
                    if (dimensionId != null) {
                        state.deathDimensions.put(uuid, ResourceKey.create(Registries.DIMENSION, dimensionId));
                    }
                } catch (IllegalArgumentException ignored) {
                    // Skip corrupt entry
                }
            }
        }

        if (tag.contains(DEATH_HOLDERS)) {
            CompoundTag holders = tag.getCompoundOrEmpty(DEATH_HOLDERS);
            for (String key : holders.keySet()) {
                try {
                    UUID ghostUuid = UUID.fromString(key);
                    // Pre-26.x saves stored holders as int-array UUIDs
                    if (holders.get(key) instanceof IntArrayTag legacyUuid) {
                        state.deathHolders.put(ghostUuid, UUIDUtil.uuidFromIntArray(legacyUuid.getAsIntArray()));
                        continue;
                    }
                    String val = holders.getStringOr(key, "");
                    if (!val.isEmpty()) {
                        state.deathHolders.put(ghostUuid, UUID.fromString(val));
                    }
                } catch (IllegalArgumentException ignored) {
                    // Skip corrupt entry
                }
            }
        }

        if (tag.contains(HEAD_BLOCK_LOCATIONS)) {
            CompoundTag headLocations = tag.getCompoundOrEmpty(HEAD_BLOCK_LOCATIONS);
            for (String key : headLocations.keySet()) {
                try {
                    UUID playerUuid = UUID.fromString(key);
                    ListTag locations = headLocations.getListOrEmpty(key);
                    List<GlobalPos> parsedLocations = new ArrayList<>();
                    for (int i = 0; i < locations.size(); i++) {
                        CompoundTag locationTag = locations.getCompoundOrEmpty(i);
                        Identifier dimensionId = Identifier.tryParse(locationTag.getStringOr(HEAD_LOCATION_DIMENSION, ""));
                        if (dimensionId == null) {
                            continue;
                        }
                        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
                        parsedLocations.add(GlobalPos.of(dimension, BlockPos.of(locationTag.getLongOr(HEAD_LOCATION_POS, 0L))));
                    }
                    if (!parsedLocations.isEmpty()) {
                        state.headBlockLocations.put(playerUuid, parsedLocations);
                    }
                } catch (IllegalArgumentException ignored) {
                    // Skip corrupt entry
                }
            }
        }

        return state;
    }

    public static final SavedDataType<GhostState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("ssoggysouls", "ghost_data"),
            GhostState::new,
            CompoundTag.CODEC.xmap(GhostState::load, GhostState::save),
            net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    // Pre-26.x saved data file name (data/ssoggysouls_ghost_data.dat)
    private static final String LEGACY_FILE_NAME = "ssoggysouls_ghost_data.dat";
    private static MinecraftServer legacyCheckedFor;

    public static GhostState getServerState(MinecraftServer server) {
        GhostState state = server.overworld().getDataStorage().computeIfAbsent(TYPE);
        if (legacyCheckedFor != server) {
            legacyCheckedFor = server;
            importLegacyData(server, state);
        }
        return state;
    }

    private static void importLegacyData(MinecraftServer server, GhostState state) {
        Path legacyFile = server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(LEGACY_FILE_NAME);
        if (!Files.exists(legacyFile)) {
            return;
        }
        try {
            CompoundTag root = NbtIo.readCompressed(legacyFile, NbtAccounter.unlimitedHeap());
            GhostState legacy = load(root.getCompoundOrEmpty("data"));
            legacy.deathLocations.forEach(state.deathLocations::putIfAbsent);
            legacy.deathDimensions.forEach(state.deathDimensions::putIfAbsent);
            legacy.deathHolders.forEach(state.deathHolders::putIfAbsent);
            legacy.headBlockLocations.forEach(state.headBlockLocations::putIfAbsent);
            state.setDirty();
            Files.move(legacyFile, legacyFile.resolveSibling(LEGACY_FILE_NAME + ".migrated"));
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(GhostState.class).error("Failed to migrate legacy ghost data from {}", legacyFile, e);
        }
    }
}
