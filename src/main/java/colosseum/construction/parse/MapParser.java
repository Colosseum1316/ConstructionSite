package colosseum.construction.parse;

import colosseum.construction.Constants;
import colosseum.construction.ConstructionSite;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.data.FinalizedMapData;
import colosseum.utility.TeamName;
import colosseum.utility.UtilWorld;
import colosseum.utility.WorldMapConstants;
import lombok.NonNull;
import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.anvil.AnvilChunk;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.material.Wool;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@SuppressWarnings({"unused", "deprecation", "FieldMayBeFinal"})
public final class MapParser implements Runnable {
    @NotNull
    public final File parsableWorldFolder;
    private final String parsableWorldPathString;

    public final FinalizedMapData mapData;

    private final Location startPoint;

    private final HashSet<Short> dataId = new HashSet<>();
    private final HashMap<String, ArrayList<Location>> teamLocations = new HashMap<>();
    private final HashMap<String, ArrayList<Location>> dataLocations = new HashMap<>();
    private final HashMap<String, ArrayList<Location>> customLocations = new HashMap<>();
    private final int radius;
    private final long wholeCubeSize;

    private long processed = 0;

    public MapParser(
            @NonNull File parsableWorldFolder,
            @NonNull FinalizedMapData mapData,
            List<String> args,
            int x, int z, int radius
    ) {
        this.parsableWorldFolder = parsableWorldFolder;
        this.parsableWorldPathString = parsableWorldFolder.getAbsolutePath();

        this.startPoint = new Location(x, 0, z);
        this.radius = radius;
        Validate.isTrue(radius > 0, "Radius must be greater than 0");
        this.wholeCubeSize = (long) (Math.pow(radius * 2 + 1, 2) * 256);

        this.mapData = mapData;
        for (String arg : args) {
            try {
                dataId.add(Short.parseShort(arg));
            } catch (NumberFormatException e) {
                ConstructionSiteProvider.getSite().getPluginLogger().log(Level.WARNING, "Invalid argument: " + arg, e);
            }
        }
    }

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

    private final AtomicReference<Status> status = new AtomicReference<>(Status.RUNNING);

    public boolean isRunning() {
        return status.get().isRunning();
    }

    public boolean isCancelled() {
        return status.get().isCancelled();
    }

    public boolean isSuccess() {
        return status.get().isSuccess();
    }

    public boolean isFail() {
        return status.get().isFail();
    }

    public void run() {
        try {
            run0();
        } catch (Exception e) {
            ConstructionSiteProvider.getSite().getPluginLogger().log(Level.SEVERE,
                    "Error while parsing " + parsableWorldPathString, e);
            status.getAndUpdate((v) -> {
                if (!v.isCancelled()) {
                    return Status.FAIL;
                }
                return v;
            });
        }
    }

    public double getProgress() {
        return 1.0 * processed / wholeCubeSize;
    }

    public void cancel() {
        status.getAndUpdate((v) -> {
            if (v.isRunning()) {
                return Status.CANCELLED;
            }
            return v;
        });
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void run0() throws Exception {
        final ConstructionSite site = ConstructionSiteProvider.getSite();
        final World world = new World(parsableWorldFolder);

        Location cornerA = null;
        Location cornerB = null;

        try (ChunkAccess<AnvilChunk> chunkAccess = world.getChunkAccess()) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                    final int blockX = startPoint.getBlockX() + offsetX;
                    final int blockZ = startPoint.getBlockZ() + offsetZ;
                    final AnvilChunk chunk = world.getChunk(chunkAccess, blockX, blockZ);

                    for (int offsetY = 0; offsetY <= 255; offsetY++) {
                        Validate.isTrue(processed <= wholeCubeSize, String.format("Overflowing: radius %d, offsetX %d, offsetY %d, offsetZ %d, processed %d, wholeCubeSize %d", radius, offsetX, offsetY, offsetZ, processed, wholeCubeSize));
                        Validate.isTrue(!this.isCancelled(), "Task is cancelled.");

                        if (processed % 10000000 == 0) {
                            site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Scanning: " + processed / 1000000 + "M of " + wholeCubeSize / 1000000 + "M");
                        }
                        processed++;

                        final int blockY = offsetY;

                        Block baseBlock;
                        try {
                            baseBlock = world.getBlock(chunk, blockX, blockY, blockZ);
                        } catch (MaterialNotFoundException e) {
                            site.getPluginLogger().log(Level.WARNING, String.format("Unknown material at %d,%d,%d, ignoring. Please double check your world save.", blockX, blockY, blockZ), e);
                            continue;
                        }

                        // ID data
                        if (dataId.contains(baseBlock.getId())) {
                            String key = "" + baseBlock.getId();
                            customLocations.computeIfAbsent(key, k -> new ArrayList<>()).add(baseBlock.getLocation());
                            continue;
                        }

                        // Signs
                        if (baseBlock.isMaterial(Material.SIGN_POST) || baseBlock.isMaterial(Material.WALL_SIGN)) {
                            Block sponge = world.getBlock(chunk, blockX, blockY - 1, blockZ);
                            if (sponge.isMaterial(Material.SPONGE)) {
                                StringBuilder name;

                                String[] signText = world.readSign(chunk, baseBlock);
                                name = new StringBuilder(signText[0]);
                                for (int signLineNum = 1; signLineNum <= 3; signLineNum++) {
                                    String l = signText[signLineNum];
                                    if (StringUtils.isNotBlank(l)) {
                                        name.append(" ").append(l);
                                    }
                                }

                                customLocations.computeIfAbsent(name.toString(), k -> new ArrayList<>()).add(sponge.getLocation());
                                world.setAir(sponge);
                                world.setAir(baseBlock);
                                continue;
                            }
                        }

                        // Tree leaves
                        // https://minecraft.fandom.com/wiki/Java_Edition_data_values/Pre-flattening#Leaves
                        // For tree leaves, you add 4 to get the no decay version.
                        if ((baseBlock.isMaterial(Material.LEAVES) || baseBlock.isMaterial(Material.LEAVES_2))
                                && baseBlock.getData() <= 3) {
                            world.setBlock(baseBlock, baseBlock.getId(), (byte) (baseBlock.getData() + 4));
                            continue;
                        }

                        // Spawns + Borders
                        if (baseBlock.isMaterial(Material.GOLD_PLATE)) {
                            Block wool = world.getBlock(chunk, blockX, blockY - 1, blockZ);
                            if (wool.isMaterial(Material.WOOL)) {
                                final byte data = wool.getData();
                                if (data == 0) {
                                    if (cornerA == null) {
                                        cornerA = wool.getLocation();
                                    } else if (cornerB == null) {
                                        cornerB = wool.getLocation();
                                    } else {
                                        site.getPluginLogger().warning("Parsing " + parsableWorldPathString + ": Found more than 2 corner markers! Known corner A: " + UtilWorld.vecToStrClean(cornerA.toVector()) + " Known corner B: " + UtilWorld.vecToStrClean(cornerB.toVector()) + " Found: " + UtilWorld.vecToStrClean(wool.getLocation().toVector()));
                                    }
                                    world.setAir(baseBlock);
                                    world.setAir(wool);
                                } else if (data >= 1 && data <= 15) {
                                    teamLocations.computeIfAbsent(TeamName.values()[data].name(), k -> new ArrayList<>()).add(wool.getLocation());
                                    world.setAir(baseBlock);
                                    world.setAir(wool);
                                } else {
                                    throw new IllegalStateException("Unexpected wool data: " + data);
                                }
                                continue;
                            }
                        }

                        if (baseBlock.isMaterial(Material.IRON_PLATE)) {
                            Block wool = world.getBlock(chunk, blockX, blockY - 1, blockZ);
                            if (wool.isMaterial(Material.WOOL)) {
                                Wool woolData = new Wool(wool.getId(), wool.getData());
                                dataLocations.computeIfAbsent(woolData.getColor().name(), k -> new ArrayList<>()).add(wool.getLocation());
                                world.setAir(baseBlock);
                                world.setAir(wool);
                            }
                        }
                    }
                }
            }

            // Finalize
            if (cornerA == null || cornerB == null) {
                site.getPluginLogger().warning("Parsing " + parsableWorldPathString + ": Corner locations are missing! Fallback to -256 to +256");
                cornerA = new Location(-256.0, 0.0, -256.0);
                cornerB = new Location(256.0, 0.0, 256.0);
            }

            world.getOfflineWorld().getLevelTag().setString(AnvilFormat.LevelTag.LEVEL_NAME, String.format("%s - %s", mapData.getMapName().orElse(Constants.UNTITLED), mapData.getMapCreator().orElse(Constants.NULL)));
            world.getOfflineWorld().saveLevelTag();

            for (Map.Entry<Pair<Integer, Integer>, AnvilChunk> c : world.getVisitedChunks().entrySet()) {
                chunkAccess.saveChunk(c.getValue());
            }

            try (FileWriter writer = new FileWriter(parsableWorldFolder.toPath().resolve(WorldMapConstants.WORLDCONFIG_DAT).toFile());
                 BufferedWriter buffer = new BufferedWriter(writer)
            ) {
                site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Writing " + WorldMapConstants.WORLDCONFIG_DAT);

                buffer.write("MAP_NAME:" + mapData.getMapName().orElse(Constants.UNTITLED));
                buffer.write("\nMAP_AUTHOR:" + mapData.getMapCreator().orElse(Constants.NULL));
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
                for (Map.Entry<String, ArrayList<Location>> stringArrayListEntry : teamLocations.entrySet()) {
                    buffer.write("\n\nTEAM_NAME:" + stringArrayListEntry.getKey());
                    buffer.write("\nTEAM_SPAWNS:" + locationsToString(stringArrayListEntry.getValue()));
                }
                // Data
                for (Map.Entry<String, ArrayList<Location>> stringArrayListEntry : dataLocations.entrySet()) {
                    buffer.write("\n\nDATA_NAME:" + stringArrayListEntry.getKey());
                    buffer.write("\nDATA_LOCS:" + locationsToString(stringArrayListEntry.getValue()));
                }
                // Custom
                for (Map.Entry<String, ArrayList<Location>> stringArrayListEntry : customLocations.entrySet()) {
                    buffer.write("\n\nCUSTOM_NAME:" + stringArrayListEntry.getKey());
                    buffer.write("\nCUSTOM_LOCS:" + locationsToString(stringArrayListEntry.getValue()));
                }
                site.getPluginLogger().info("Parsing " + parsableWorldPathString + ": Successfully created " + WorldMapConstants.WORLDCONFIG_DAT);
            }
            status.set(Status.SUCCESS);
        }
    }

    private String locationsToString(List<Location> locs) {
        return String.join(Constants.LOCATIONS_DELIMITER, locs.stream().map(loc -> String.format("%d,%d,%d", (int) loc.getX(), (int) loc.getY(), (int) loc.getZ())).toArray(String[]::new));
    }
}
