package colosseum.construction;

import colosseum.construction.data.FinalizedMapData;
import colosseum.construction.manager.MapDataManager;
import colosseum.utility.UtilMath;
import colosseum.utility.UtilWorld;
import colosseum.utility.WorldMapConstants;
import colosseum.utility.arcade.GameType;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
    private final List<String> args;
    private final Vector startPoint;

    private final HashSet<Short> dataId = new HashSet<>();
    private final HashMap<String, ArrayList<Vector>> teamLocations = new HashMap<>();
    private final HashMap<String, ArrayList<Vector>> dataLocations = new HashMap<>();
    private final HashMap<String, ArrayList<Vector>> customLocations = new HashMap<>();
    private final int size;
    private final int wholeCubeSize;

    private long processed = 0;

    public enum Status {
        SUCCESS,
        FAIL,
        RUNNING;

        public boolean isDone() {
            return this != RUNNING;
        }

        public boolean isSuccess() {
            return this == SUCCESS;
        }

        public boolean isFail() {
            return this == FAIL;
        }
    }

    @Getter
    @SuppressWarnings("WriteOnlyObject")
    private final AtomicReference<Status> status = new AtomicReference<>(Status.RUNNING);

    public MapParser(File parsableWorldFolder, List<String> args, Location startPoint) {
        this(parsableWorldFolder, args, startPoint, 600);
    }

    public MapParser(@NotNull File parsableWorldFolder, List<String> args, Location startPoint, int size) {
        this.parsableWorldFolder = parsableWorldFolder;
        this.parsableWorldPathString = parsableWorldFolder.getAbsolutePath();
        this.args = List.copyOf(args);
        this.startPoint = new Vector(startPoint.getX(), startPoint.getY(), startPoint.getZ());
        this.size = size;
        this.wholeCubeSize = ((size * 2) * (size * 2) * 256);
        Validate.isTrue(size > 0, "size must be greater than 0");
        this.mapData = ConstructionSiteProvider.getSite().getManager(MapDataManager.class).getFinalized(parsableWorldFolder);
        for (String arg : args) {
            try {
                dataId.add(Short.parseShort(arg));
            } catch (NumberFormatException e) {
                ConstructionSiteProvider.getSite().getPluginLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private Chunk getChunk(ChunkAccess<?> chunkAccess, int blockX, int blockZ) throws IOException {
        return chunkAccess.getChunk(Math.floorDiv(blockX, AnvilChunk.CHUNK_X_SIZE), Math.floorDiv(blockZ, AnvilChunk.CHUNK_Z_SIZE));
    }

    private WrappedBaseBlock getBlockBare(World offlineWorld, Chunk chunk, int blockX, int blockY, int blockZ) {
        return getBlockBare(offlineWorld, chunk.getMaterial(Math.floorMod(blockX, AnvilChunk.CHUNK_X_SIZE), blockY, Math.floorMod(blockZ, AnvilChunk.CHUNK_Z_SIZE)), blockX, blockY, blockZ);
    }

    private WrappedBaseBlock getBlockBare(World offlineWorld, MaterialData materialData, int blockX, int blockY, int blockZ) {
        char id = ((AnvilMaterialMap) offlineWorld.getGameFactory().getMaterialMap()).getOldMinecraftId(materialData);
        short typeId = (short) (id >> 4);
        byte data = (byte) (id & 0xF);

        return new WrappedBaseBlock(materialData, typeId, data, blockX, blockY, blockZ);
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

    private void setAir(World offlineWorld, Chunk chunk, WrappedBaseBlock block) {
        setBlock(chunk, block, offlineWorld.getGameFactory().getMaterialMap().getGlobal().getAir());
    }

    private void setBlock(World offlineWorld, Chunk chunk, int blockX, int blockY, int blockZ, short typeId, byte data) {
        chunk.setMaterial(Math.floorMod(blockX, AnvilChunk.CHUNK_X_SIZE), blockY, Math.floorMod(blockZ, AnvilChunk.CHUNK_Z_SIZE), ((AnvilMaterialMap) offlineWorld.getGameFactory().getMaterialMap()).getMaterialDataFromOldIds(typeId, data));
    }

    private void setBlock(Chunk chunk, WrappedBaseBlock block, MaterialData newMaterialData) {
        chunk.setMaterial(Math.floorMod(block.x, AnvilChunk.CHUNK_X_SIZE), block.y, Math.floorMod(block.z, AnvilChunk.CHUNK_Z_SIZE), newMaterialData);
    }

    @AllArgsConstructor
    private static final class WrappedBaseBlock {
        private final MaterialData materialData;
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
        final World offlineWorld;
        try {
            offlineWorld = new AnvilWorld(new GlobalMaterialMap(), parsableWorldFolder.toPath().resolve(AnvilWorld.LEVEL_DAT_NAME));
        } catch (Exception e) {
            ConstructionSiteProvider.getSite().getPluginLogger().log(Level.SEVERE, "Error while parsing " + parsableWorldPathString, e);
            status.set(Status.FAIL);
            return;
        }

        Vector cornerA = null;
        Vector cornerB = null;

        try (ChunkAccess<AnvilChunk> chunkAccess = (ChunkAccess<AnvilChunk>) offlineWorld.getChunkAccess()) {
            int offsetX;
            for (offsetX = -size; offsetX <= size; offsetX++) {
                int offsetZ;
                for (offsetZ = -size; offsetZ <= size; offsetZ++) {
                    final int blockX = startPoint.getBlockX() + offsetX;
                    final int blockZ = startPoint.getBlockZ() + offsetZ;
                    final Chunk chunk = getChunk(chunkAccess, blockX, blockZ);

                    int offsetY;
                    for (offsetY = 0; offsetY <= 255; offsetY++) {
                        if (processed % 10000000 == 0) {
                            ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Scanning: " + processed / 1000000 + "M of " + wholeCubeSize / 1000000 + "M");
                        }
                        processed++;

                        final int blockY = offsetY;

                        WrappedBaseBlock wrappedObject = getBlockBare(offlineWorld, chunk, blockX, blockY, blockZ);

                        // ID data
                        if (dataId.contains(wrappedObject.typeId)) {
                            String key = "" + wrappedObject.typeId;
                            customLocations.computeIfAbsent(key, k -> new ArrayList<>()).add(wrappedObject.getLocation());
                            ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Found data id " + key + " at " + UtilWorld.vecToStrClean(wrappedObject.getLocation()));
                            continue;
                        }

                        if (wrappedObject.isMaterial(Material.GLASS)) {
                            if (mapData.getMapGameType().equals(GameType.MicroBattle)) {
                                String name = "20"; // Set auto glass
                                customLocations.computeIfAbsent(name, k -> new ArrayList<>()).add(wrappedObject.getLocation());
                                ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Set Auto glass at " + UtilWorld.vecToStrClean(wrappedObject.getLocation()));
                                setAir(offlineWorld, chunk, wrappedObject);
                            }
                        }

                        // Signs
                        if (wrappedObject.isMaterial(Material.SIGN_POST) || wrappedObject.isMaterial(Material.WALL_SIGN)) {
                            WrappedBaseBlock wrappedBlockSponge = getBlockBare(offlineWorld, chunk, blockX, blockY - 1, blockZ);
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
                                    ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Found custom location: \"" + name + "\" at " + UtilWorld.vecToStrClean(wrappedBlockSponge.getLocation()));
                                } catch (Exception e) {
                                    ConstructionSiteProvider.getSite().getPluginLogger().warning(String.format("Parsing " + parsableWorldPathString + ": Found invalid sign data at %d,%d,%d", wrappedObject.x, wrappedObject.y, wrappedObject.z));
                                    ConstructionSiteProvider.getSite().getPluginLogger().log(Level.WARNING, e.getMessage(), e);
                                }

                                customLocations.computeIfAbsent(name.toString(), k -> new ArrayList<>()).add(wrappedBlockSponge.getLocation());
                                setAir(offlineWorld, chunk, wrappedBlockSponge);
                                setAir(offlineWorld, chunk, wrappedObject);
                            }
                        }
                        if (wrappedObject.isMaterial(Material.LEAVES) || wrappedObject.isMaterial(Material.LEAVES_2)) {
                            if (wrappedObject.data <= 3) {
                                // https://minecraft.fandom.com/wiki/Java_Edition_data_values/Pre-flattening#Leaves
                                // For tree leaves, you add 4 to get the no decay version.
                                setBlock(offlineWorld, chunk, wrappedObject.x, wrappedObject.y, wrappedObject.z, wrappedObject.typeId, (byte) (wrappedObject.data + 4));
                            }
                        }

                        // Spawns + Borders
                        if (wrappedObject.isMaterial(Material.GOLD_PLATE)) {
                            WrappedBaseBlock wrappedBlockWool = getBlockBare(offlineWorld, chunk, blockX, blockY - 1, blockZ);
                            if (wrappedBlockWool.isMaterial(Material.WOOL)) {
                                switch (wrappedBlockWool.data) {
                                    case 0 -> {
                                        if (cornerA == null) {
                                            cornerA = wrappedBlockWool.getLocation().clone();
                                            ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Set corner A: " + UtilWorld.vecToStrClean(cornerA));
                                        } else if (cornerB == null) {
                                            cornerB = wrappedBlockWool.getLocation().clone();
                                            ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Set corner B: " + UtilWorld.vecToStrClean(cornerB));
                                        } else {
                                            ConstructionSiteProvider.getSite().getPluginLogger().warning("Parsing " + parsableWorldPathString + ": Found more than 2 corner markers! Known corner A: " + UtilWorld.vecToStrClean(cornerA) + " Known corner B: " + UtilWorld.vecToStrClean(cornerB) + " Found: " + UtilWorld.vecToStrClean(wrappedBlockWool.getLocation()));
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
                                }
                            }
                        }

                        if (!wrappedObject.isMaterial(Material.IRON_PLATE)) {
                            continue;
                        }

                        WrappedBaseBlock wrappedBlockWool = getBlockBare(offlineWorld, chunk, blockX, blockY - 1, blockZ);
                        if (!wrappedBlockWool.isMaterial(Material.WOOL)) {
                            continue;
                        }
                        Wool woolData = new Wool(wrappedBlockWool.typeId, wrappedBlockWool.data);
                        dataLocations.computeIfAbsent(woolData.getColor().name(), k -> new ArrayList<>()).add(wrappedBlockWool.getLocation());
                        ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Found data location at " + UtilWorld.vecToStrClean(wrappedBlockWool.getLocation()));
                        setAir(offlineWorld, chunk, wrappedObject);
                        setAir(offlineWorld, chunk, wrappedBlockWool);
                    }
                    chunkAccess.saveChunk((AnvilChunk) chunk);
                }
            }

            // Finalize
            if (cornerA == null || cornerB == null) {
                ConstructionSiteProvider.getSite().getPluginLogger().warning("Parsing " + parsableWorldPathString + ": Corner locations are missing! Fallback to -256 to +256");
                cornerA = new Vector(-256.0, 0.0, -256.0);
                cornerB = new Vector(256.0, 0.0, 256.0);
                ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Set corner A: " + UtilWorld.vecToStrClean(cornerA));
                ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Set corner B: " + UtilWorld.vecToStrClean(cornerB));
            }

            offlineWorld.getLevelTag().setString(AnvilFormat.LevelTag.LEVEL_NAME, mapData.getMapName() + " - " + mapData.getMapCreator() + " (" + mapData.getMapGameType().name() + ")");
            offlineWorld.saveLevelTag();

            try (
                    FileWriter writer = new FileWriter(parsableWorldFolder.toPath().resolve(WorldMapConstants.WORLDCONFIG_DAT).toFile());
                    BufferedWriter buffer = new BufferedWriter(writer)
            ) {
                ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Writing " + WorldMapConstants.WORLDCONFIG_DAT);

                buffer.write("MAP_NAME:" + mapData.getMapName());
                buffer.write("\nMAP_AUTHOR:" + mapData.getMapCreator());
                buffer.write("\n\nMIN_X:" + UtilMath.getMin(cornerA.getBlockX(), cornerB.getBlockX()));
                buffer.write("\nMAX_X:" + UtilMath.getMax(cornerA.getBlockX(), cornerB.getBlockX()));
                buffer.write("\nMIN_Z:" + UtilMath.getMin(cornerA.getBlockZ(), cornerB.getBlockZ()));
                buffer.write("\nMAX_Z:" + UtilMath.getMax(cornerA.getBlockZ(), cornerB.getBlockZ()));
                if (cornerA.getBlockY() == cornerB.getBlockY()) {
                    buffer.write("\nMIN_Y:0");
                    buffer.write("\nMAX_Y:256");
                } else {
                    buffer.write("\nMIN_Y:" + UtilMath.getMin(cornerA.getBlockY(), cornerB.getBlockY()));
                    buffer.write("\nMAX_Y:" + UtilMath.getMax(cornerA.getBlockY(), cornerB.getBlockY()));
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
                ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Successfully created " + WorldMapConstants.WORLDCONFIG_DAT);
            }
        } catch (Exception e) {
            ConstructionSiteProvider.getSite().getPluginLogger().log(Level.SEVERE, "Error while parsing " + parsableWorldPathString, e);
            status.set(Status.FAIL);
            return;
        }
        status.set(Status.SUCCESS);
    }

    private String locationsToString(List<Vector> locs) {
        return String.join(";", locs.stream().map(loc -> String.format("%d,%d,%d", (int) loc.getX(), (int) loc.getY(), (int) loc.getZ())).toArray(String[]::new));
    }

    private void setTeamLocations(String key, WrappedBaseBlock block, WrappedBaseBlock wool, World offlineWorld, Chunk chunk) {
        ConstructionSiteProvider.getSite().getPluginLogger().info("Parsing " + parsableWorldPathString + ": Found team location: " + key + " at " + UtilWorld.vecToStrClean(wool.getLocation()));
        teamLocations.computeIfAbsent(key, k -> new ArrayList<>()).add(wool.getLocation());
        setAir(offlineWorld, chunk, block);
        setAir(offlineWorld, chunk, wool);
    }
}
