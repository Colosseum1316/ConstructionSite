package colosseum.construction;

import colosseum.construction.data.FinalizedMapData;
import colosseum.utility.UtilWorld;
import colosseum.utility.WorldMapConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.World;
import nl.rutgerkok.hammer.anvil.AnvilChunk;
import nl.rutgerkok.hammer.anvil.AnvilMaterialMap;
import nl.rutgerkok.hammer.anvil.AnvilWorld;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundTag;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.material.Wool;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@SuppressWarnings({"unused", "deprecation", "FieldMayBeFinal", "ClassCanBeRecord"})
public final class MapParser implements Runnable {
    @NotNull
    public final File parsableWorldFolder;
    private final String parsableWorldPathString;
    public final FinalizedMapData mapData;
    private final Vector startPoint;

    private final HashSet<Short> dataId = new HashSet<>();
    private final HashMap<String, ArrayList<Vector>> teamLocations = new HashMap<>();
    private final HashMap<String, ArrayList<Vector>> dataLocations = new HashMap<>();
    private final HashMap<String, ArrayList<Vector>> customLocations = new HashMap<>();
    private final int radius;
    private final long wholeCubeSize;

    private long processed = 0;

    public enum Status {
        SUCCESS,
        FAIL,
        CANCELLED,
        RUNNING;

        public boolean isRunning() {
            return this == RUNNING;
        }

        public boolean isCancelled() {
            return this == CANCELLED;
        }

        public boolean isSuccess() {
            return this == SUCCESS;
        }

        public boolean isFail() {
            return this == FAIL || this == CANCELLED;
        }
    }

    @Getter
    @SuppressWarnings("WriteOnlyObject")
    private final AtomicReference<Status> status = new AtomicReference<>(Status.RUNNING);

    public MapParser(@NonNull File parsableWorldFolder, @NonNull FinalizedMapData mapData, List<String> args, Location startPoint, int radius) {
        this.parsableWorldFolder = parsableWorldFolder;
        this.parsableWorldPathString = parsableWorldFolder.getAbsolutePath();
        this.startPoint = new Vector(startPoint.getX(), startPoint.getY(), startPoint.getZ());
        this.radius = radius;
        Validate.isTrue(radius > 0, "Radius must be greater than 0");
        this.wholeCubeSize = (long) (Math.pow(radius * 2 + 1, 2) * 256);
        ConstructionSite site = ConstructionSiteProvider.getSite();
        this.mapData = mapData;
        for (String arg : args) {
            try {
                dataId.add(Short.parseShort(arg));
            } catch (NumberFormatException e) {
                site.getPluginLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private Chunk getChunk(ChunkAccess<?> chunkAccess, int blockX, int blockZ) throws IOException {
        return chunkAccess.getChunk(Math.floorDiv(blockX, AnvilChunk.CHUNK_X_SIZE), Math.floorDiv(blockZ, AnvilChunk.CHUNK_Z_SIZE));
    }

    private BareBlock getBareBlock(World offlineWorld, Chunk chunk, int blockX, int blockY, int blockZ) {
        return getBareBlock(offlineWorld, chunk.getMaterial(Math.floorMod(blockX, AnvilChunk.CHUNK_X_SIZE), blockY, Math.floorMod(blockZ, AnvilChunk.CHUNK_Z_SIZE)), blockX, blockY, blockZ);
    }

    private BareBlock getBareBlock(World offlineWorld, MaterialData materialData, int blockX, int blockY, int blockZ) {
        char id = ((AnvilMaterialMap) offlineWorld.getGameFactory().getMaterialMap()).getOldMinecraftId(materialData);
        short typeId = (short) (id >> 4);
        byte data = (byte) (id & 0xF);

        return new BareBlock(typeId, data, blockX, blockY, blockZ);
    }

    /**
     * <b>This method blindly trusts input!!!</b>
     */
    private String[] readSign(Chunk chunk, int blockX, int blockY, int blockZ) {
        String[] res = new String[]{"", "", "", ""};
        for (CompoundTag tileEntity : chunk.getTileEntities()) {
            if (tileEntity.getInt(AnvilFormat.TileEntityTag.X_POS) == blockX
                && tileEntity.getInt(AnvilFormat.TileEntityTag.Y_POS) == blockY
                && tileEntity.getInt(AnvilFormat.TileEntityTag.Z_POS) == blockZ) {
                for (int i = 0; i < 4; i++) {
                    res[i] = Objects.requireNonNullElse(tileEntity.getString(AnvilFormat.TileEntityTag.SIGN_LINE_NAMES.get(i)), res[i]);
                }
                break;
            }
        }
        return res;
    }

    private void setBlock(World offlineWorld, Chunk chunk, short typeId, byte data, int blockX, int blockY, int blockZ) {
        setBlock(offlineWorld, chunk, new BareBlock(typeId, data, blockX, blockY, blockZ));
    }

    private void setBlock(World offlineWorld, Chunk chunk, BareBlock block) {
        chunk.setMaterial(Math.floorMod(block.x, AnvilChunk.CHUNK_X_SIZE), block.y, Math.floorMod(block.z, AnvilChunk.CHUNK_Z_SIZE), ((AnvilMaterialMap) offlineWorld.getGameFactory().getMaterialMap()).getMaterialDataFromOldIds(block.typeId, block.data));
    }

    private void setBlock(Chunk chunk, BareBlock block, MaterialData newMaterialData) {
        chunk.setMaterial(Math.floorMod(block.x, AnvilChunk.CHUNK_X_SIZE), block.y, Math.floorMod(block.z, AnvilChunk.CHUNK_Z_SIZE), newMaterialData);
    }

    private void setAir(World offlineWorld, Chunk chunk, BareBlock block) {
        setBlock(chunk, block, offlineWorld.getGameFactory().getMaterialMap().getGlobal().getAir());
    }

    @AllArgsConstructor
    private static final class BareBlock {
        private final short typeId;
        private final byte data;
        private final int x;
        private final int y;
        private final int z;

        private Vector getLocation() {
            return new Vector(x, y, z);
        }

        private boolean isMaterial(Material material) {
            return typeId == material.getId();
        }
    }

    public double getProgress() {
        return 1.0 * processed / wholeCubeSize;
    }

    public void run() {
        final ConstructionSite site = ConstructionSiteProvider.getSite();
        final World offlineWorld;
        try {
            offlineWorld = new AnvilWorld(new GlobalMaterialMap(), parsableWorldFolder.toPath().resolve(AnvilWorld.LEVEL_DAT_NAME));
        } catch (Exception e) {
            site.getPluginLogger().log(Level.SEVERE, "Error while parsing " + parsableWorldPathString, e);
            status.set(Status.FAIL);
            return;
        }

        Vector cornerA = null;
        Vector cornerB = null;

        try (ChunkAccess<AnvilChunk> chunkAccess = (ChunkAccess<AnvilChunk>) offlineWorld.getChunkAccess()) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                    final int blockX = startPoint.getBlockX() + offsetX;
                    final int blockZ = startPoint.getBlockZ() + offsetZ;
                    final Chunk chunk = getChunk(chunkAccess, blockX, blockZ);
                    boolean updated = false;

                    for (int offsetY = 0; offsetY <= 255; offsetY++) {
                        Validate.isTrue(processed <= wholeCubeSize, String.format("Overflowing: radius %d, offsetX %d, offsetY %d, offsetZ %d, processed %d, wholeCubeSize %d", radius, offsetX, offsetY, offsetZ, processed, wholeCubeSize));
                        Validate.isTrue(!status.get().isCancelled(), "Task is cancelled.");
                        if (processed % 10000000 == 0) {
                            site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Scanning: " + processed / 1000000 + "M of " + wholeCubeSize / 1000000 + "M");
                        }
                        processed++;

                        final int blockY = offsetY;

                        BareBlock wrappedObject = getBareBlock(offlineWorld, chunk, blockX, blockY, blockZ);

                        // ID data
                        if (dataId.contains(wrappedObject.typeId)) {
                            String key = "" + wrappedObject.typeId;
                            customLocations.computeIfAbsent(key, k -> new ArrayList<>()).add(wrappedObject.getLocation());
                            site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Found data id " + key + " at " + UtilWorld.vecToStrClean(wrappedObject.getLocation()));
                            continue;
                        }

                        // Signs
                        if (wrappedObject.isMaterial(Material.SIGN_POST) || wrappedObject.isMaterial(Material.WALL_SIGN)) {
                            BareBlock wrappedBlockSponge = getBareBlock(offlineWorld, chunk, blockX, blockY - 1, blockZ);
                            if (wrappedBlockSponge.isMaterial(Material.SPONGE)) {
                                StringBuilder name = new StringBuilder();
                                try {
                                    String[] signText = readSign(chunk, wrappedObject.x, wrappedObject.y, wrappedObject.z);
                                    name = new StringBuilder(signText[0]);
                                    for (int signLineNum = 1; signLineNum <= 3; signLineNum++) {
                                        String l = signText[signLineNum];
                                        if (StringUtils.isNotBlank(l)) {
                                            name.append(" ").append(l);
                                        }
                                    }
                                    site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Found custom location: \"" + name + "\" at " + UtilWorld.vecToStrClean(wrappedBlockSponge.getLocation()));
                                } catch (Exception e) {
                                    site.getPluginLogger().warning(String.format("Parsing " + parsableWorldPathString + ": Found invalid sign data at %d,%d,%d", wrappedObject.x, wrappedObject.y, wrappedObject.z));
                                    site.getPluginLogger().log(Level.WARNING, e.getMessage(), e);
                                }

                                customLocations.computeIfAbsent(name.toString(), k -> new ArrayList<>()).add(wrappedBlockSponge.getLocation());
                                setAir(offlineWorld, chunk, wrappedBlockSponge);
                                setAir(offlineWorld, chunk, wrappedObject);
                                updated = true;
                                continue;
                            }
                        }
                        if (wrappedObject.isMaterial(Material.LEAVES) || wrappedObject.isMaterial(Material.LEAVES_2)) {
                            if (wrappedObject.data <= 3) {
                                // https://minecraft.fandom.com/wiki/Java_Edition_data_values/Pre-flattening#Leaves
                                // For tree leaves, you add 4 to get the no decay version.
                                setBlock(offlineWorld, chunk, new BareBlock(wrappedObject.typeId, (byte) (wrappedObject.data + 4), wrappedObject.x, wrappedObject.y, wrappedObject.z));
                                updated = true;
                                continue;
                            }
                        }

                        // Spawns + Borders
                        if (wrappedObject.isMaterial(Material.GOLD_PLATE)) {
                            BareBlock wrappedBlockWool = getBareBlock(offlineWorld, chunk, blockX, blockY - 1, blockZ);
                            if (wrappedBlockWool.isMaterial(Material.WOOL)) {
                                switch (wrappedBlockWool.data) {
                                    case 0 -> {
                                        if (cornerA == null) {
                                            cornerA = wrappedBlockWool.getLocation().clone();
                                            site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Set corner A: " + UtilWorld.vecToStrClean(cornerA));
                                        } else if (cornerB == null) {
                                            cornerB = wrappedBlockWool.getLocation().clone();
                                            site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Set corner B: " + UtilWorld.vecToStrClean(cornerB));
                                        } else {
                                            site.getPluginLogger().warning("Parsing " + parsableWorldPathString + ": Found more than 2 corner markers! Known corner A: " + UtilWorld.vecToStrClean(cornerA) + " Known corner B: " + UtilWorld.vecToStrClean(cornerB) + " Found: " + UtilWorld.vecToStrClean(wrappedBlockWool.getLocation()));
                                        }
                                        setAir(offlineWorld, chunk, wrappedObject);
                                        setAir(offlineWorld, chunk, wrappedBlockWool);
                                    }
                                    case 1 -> setTeamLocations("Orange", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 2 -> setTeamLocations("Magenta", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 3 -> setTeamLocations("Sky", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 4 -> setTeamLocations("Yellow", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 5 -> setTeamLocations("Lime", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 6 -> setTeamLocations("Pink", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 7 -> setTeamLocations("Gray", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 8 -> setTeamLocations("LGray", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 9 -> setTeamLocations("Cyan", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 10 -> setTeamLocations("Purple", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 11 -> setTeamLocations("Blue", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 12 -> setTeamLocations("Brown", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 13 -> setTeamLocations("Green", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 14 -> setTeamLocations("Red", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    case 15 -> setTeamLocations("Black", wrappedObject, wrappedBlockWool, offlineWorld, chunk);
                                    default -> throw new IllegalStateException("Unexpected wool data: " + wrappedBlockWool.data);
                                }
                                updated = true;
                                continue;
                            }
                        }

                        if (wrappedObject.isMaterial(Material.IRON_PLATE)) {
                            BareBlock wrappedBlockWool = getBareBlock(offlineWorld, chunk, blockX, blockY - 1, blockZ);
                            if (wrappedBlockWool.isMaterial(Material.WOOL)) {
                                Wool woolData = new Wool(wrappedBlockWool.typeId, wrappedBlockWool.data);
                                dataLocations.computeIfAbsent(woolData.getColor().name(), k -> new ArrayList<>()).add(wrappedBlockWool.getLocation());
                                site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Found data location at " + UtilWorld.vecToStrClean(wrappedBlockWool.getLocation()));
                                setAir(offlineWorld, chunk, wrappedObject);
                                setAir(offlineWorld, chunk, wrappedBlockWool);
                                updated = true;
                            }
                        }
                    }
                    if (updated) {
                        chunkAccess.saveChunk((AnvilChunk) chunk);
                    }
                }
            }

            // Finalize
            if (cornerA == null || cornerB == null) {
                site.getPluginLogger().warning("Parsing " + parsableWorldPathString + ": Corner locations are missing! Fallback to -256 to +256");
                cornerA = new Vector(-256.0, 0.0, -256.0);
                cornerB = new Vector(256.0, 0.0, 256.0);
                site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Set corner A: " + UtilWorld.vecToStrClean(cornerA));
                site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Set corner B: " + UtilWorld.vecToStrClean(cornerB));
            }

            offlineWorld.getLevelTag().setString(AnvilFormat.LevelTag.LEVEL_NAME, mapData.getMapName().get() + " - " + mapData.getMapCreator().get() + " (" + mapData.getMapGameType().get().name() + ")");
            offlineWorld.saveLevelTag();

            try (
                    FileWriter writer = new FileWriter(parsableWorldFolder.toPath().resolve(WorldMapConstants.WORLDCONFIG_DAT).toFile());
                    BufferedWriter buffer = new BufferedWriter(writer)
            ) {
                site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Writing " + WorldMapConstants.WORLDCONFIG_DAT);

                buffer.write("MAP_NAME:" + mapData.getMapName().get());
                buffer.write("\nMAP_AUTHOR:" + mapData.getMapCreator().get());
                buffer.write("\n\nMIN_X:" + Math.min(cornerA.getBlockX(), cornerB.getBlockX()));
                buffer.write("\nMAX_X:" + Math.max(cornerA.getBlockX(), cornerB.getBlockX()));
                buffer.write("\nMIN_Z:" + Math.min(cornerA.getBlockZ(), cornerB.getBlockZ()));
                buffer.write("\nMAX_Z:" + Math.max(cornerA.getBlockZ(), cornerB.getBlockZ()));
                if (cornerA.getBlockY() == cornerB.getBlockY()) {
                    buffer.write("\nMIN_Y:0");
                    buffer.write("\nMAX_Y:256");
                } else {
                    buffer.write("\nMIN_Y:" + Math.min(cornerA.getBlockY(), cornerB.getBlockY()));
                    buffer.write("\nMAX_Y:" + Math.max(cornerA.getBlockY(), cornerB.getBlockY()));
                }

                // Teams
                for (Map.Entry<String, ArrayList<Vector>> stringArrayListEntry : teamLocations.entrySet()) {
                    buffer.write("\n\nTEAM_NAME:" + stringArrayListEntry.getKey());
                    buffer.write("\nTEAM_SPAWNS:" + locationsToString(stringArrayListEntry.getValue()));
                }
                // Data
                for (Map.Entry<String, ArrayList<Vector>> stringArrayListEntry : dataLocations.entrySet()) {
                    buffer.write("\n\nDATA_NAME:" + stringArrayListEntry.getKey());
                    buffer.write("\nDATA_LOCS:" + locationsToString(stringArrayListEntry.getValue()));
                }
                // Custom
                for (Map.Entry<String, ArrayList<Vector>> stringArrayListEntry : customLocations.entrySet()) {
                    buffer.write("\n\nCUSTOM_NAME:" + stringArrayListEntry.getKey());
                    buffer.write("\nCUSTOM_LOCS:" + locationsToString(stringArrayListEntry.getValue()));
                }
                site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Successfully created " + WorldMapConstants.WORLDCONFIG_DAT);
            }
        } catch (Exception e) {
            site.getPluginLogger().log(Level.SEVERE, "Error while parsing " + parsableWorldPathString, e);
            status.getAndUpdate((v) -> {
                if (!v.isCancelled()) {
                    return Status.FAIL;
                }
                return v;
            });
            return;
        }
        status.set(Status.SUCCESS);
    }

    private String locationsToString(List<Vector> locs) {
        return String.join(Constants.LOCATIONS_DELIMITER, locs.stream().map(loc -> String.format("%d,%d,%d", (int) loc.getX(), (int) loc.getY(), (int) loc.getZ())).toArray(String[]::new));
    }

    private void setTeamLocations(String key, BareBlock block, BareBlock wool, World offlineWorld, Chunk chunk) {
        ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Found team location: " + key + " at " + UtilWorld.vecToStrClean(wool.getLocation()));
        teamLocations.computeIfAbsent(key, k -> new ArrayList<>()).add(wool.getLocation());
        setAir(offlineWorld, chunk, block);
        setAir(offlineWorld, chunk, wool);
    }
}
