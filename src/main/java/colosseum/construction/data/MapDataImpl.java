package colosseum.construction.data;

import colosseum.construction.Constants;
import colosseum.construction.ConstructionSite;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.GameTypeUtils;
import colosseum.construction.WorldUtils;
import colosseum.utility.UtilWorld;
import colosseum.utility.WorldMapConstants;
import colosseum.utility.arcade.GameType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MapDataImpl extends AbstractMapData implements MutableMapData {
    protected File datFile;

    @Getter
    protected String mapName;
    @Getter
    protected String mapCreator;
    @Getter
    protected GameType mapGameType;
    protected final Map<String, Vector> warps;
    protected final Set<UUID> adminList;
    @Getter
    protected boolean live;

    private final Object lock;

    public MapDataImpl(@Nullable World world, @NotNull File worldFolder) {
        super(world, worldFolder);
        this.warps = Maps.newConcurrentMap();
        this.adminList = Sets.newConcurrentHashSet();
        this.lock = new Object();
        init();
    }

    protected void init() {
        if (world != null) {
            Validate.isTrue(WorldUtils.getWorldFolder(world).equals(worldFolder));
        }
        this.datFile = worldFolder.toPath().resolve(WorldMapConstants.MAP_DAT).toFile();
        if (this.datFile.exists()) {
            read();
        } else {
            ConstructionSiteProvider.getSite().getPluginLogger().warning(String.format("There's no \"%s\" in \"%s\"! Creating a dummy one. Please set map data accordingly.", WorldMapConstants.MAP_DAT, worldFolder.getAbsolutePath()));
            update(new FinalizedMapData("MapName", "MapCreator", GameType.None, ImmutableMap.of(), ImmutableSet.of(), true));
            write();
        }
    }

    protected void read() {
        final ConstructionSite site = ConstructionSiteProvider.getSite();
        synchronized (lock) {
            String line;

            try (BufferedReader br = Files.newBufferedReader(datFile.toPath(), StandardCharsets.UTF_8)) {
                while ((line = br.readLine()) != null) {
                    List<String> tokens = Arrays.stream(line.split(":")).collect(Collectors.toList());
                    if (tokens.size() < 2) {
                        continue;
                    }
                    if (StringUtils.isEmpty(tokens.get(0))) {
                        continue;
                    }
                    switch (tokens.get(0)) {
                        case "currentlyLive": {
                            live = Boolean.parseBoolean(tokens.get(1));
                            break;
                        }
                        case "warps": {
                            for (String w : tokens.get(1).split(Constants.LOCATIONS_DELIMITER)) {
                                String[] entry = w.split("@");
                                Validate.isTrue(entry.length >= 2);
                                String[] xyz = entry[1].replaceAll("[()]", "").split(",");
                                warps.computeIfAbsent(entry[0], k -> new Vector(Double.parseDouble(xyz[0]), Double.parseDouble(xyz[1]), Double.parseDouble(xyz[2])));
                            }
                            break;
                        }
                        case "MAP_NAME": {
                            mapName = tokens.get(1);
                            break;
                        }
                        case "MAP_AUTHOR":
                        case "MAP_CREATOR": {
                            mapCreator = tokens.get(1);
                            break;
                        }
                        case "GAME_TYPE": {
                            mapGameType = GameTypeUtils.determineGameType(tokens.get(1), true);
                            break;
                        }
                        case "ADMIN_LIST":
                        case "BUILD_LIST": {
                            adminList.addAll(Arrays.stream(tokens.get(1).split(",")).map(UUID::fromString).collect(Collectors.toList()));
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                site.getPluginLogger().log(Level.SEVERE, "Cannot read dat file!", e);
            }
        }
        if (mapGameType == null) {
            site.getPluginLogger().warning("World " + worldFolder.getAbsolutePath() + " has malformed GameType info. Fall back to None.");
            mapGameType = GameType.None;
        }
        if (mapGameType.equals(GameType.None)) {
            site.getPluginLogger().warning("World " + worldFolder.getAbsolutePath() + " has a \"None\" GameType!");
        }
    }

    @Override
    public void update(FinalizedMapData newMapData) {
        synchronized (lock) {
            this.mapName = newMapData.getMapName().orElse(this.mapName);
            this.mapCreator = newMapData.getMapCreator().orElse(this.mapCreator);
            this.mapGameType = newMapData.getMapGameType().orElse(this.mapGameType);
            newMapData.getWarps().ifPresent(w -> {
                this.warps.clear();
                this.warps.putAll(w);
            });
            newMapData.getAdminList().ifPresent(a -> {
                this.adminList.clear();
                this.adminList.addAll(a);
            });
            this.live = newMapData.getLive().orElse(this.live);
        }
    }

    @Override
    public boolean write() {
        synchronized (lock) {
            String mapName = this.mapName;
            String mapCreator = this.mapCreator;
            GameType mapGameType = this.mapGameType;
            boolean currentlyLive = this.live;
            ImmutableSet<UUID> adminList = adminList();
            ImmutableMap<String, Vector> warps = warps();
            final ConstructionSite site = ConstructionSiteProvider.getSite();
            try (BufferedWriter buffer = Files.newBufferedWriter(datFile.toPath(), StandardCharsets.UTF_8)) {
                site.getPluginLogger().info("Writing " + datFile.getAbsolutePath());
                buffer.write("MAP_NAME:" + mapName);
                buffer.write("\nMAP_AUTHOR:" + mapCreator);
                buffer.write("\nGAME_TYPE:" + mapGameType);
                buffer.write("\nADMIN_LIST:" + adminList.stream().map(UUID::toString).collect(Collectors.joining(",")));
                buffer.write("\ncurrentlyLive:" + currentlyLive);
                buffer.write("\nwarps:" + warpsToString(warps));
                return true;
            } catch (IOException e) {
                site.getPluginLogger().log(Level.SEVERE, "Cannot write dat file!", e);
                FileUtils.deleteQuietly(datFile);
                return false;
            }
        }
    }

    @Override
    public ImmutableMap<String, Vector> warps() {
        ImmutableMap.Builder<String, Vector> builder = ImmutableMap.builder();
        warps.forEach((s, l) -> builder.put(s, l.clone()));
        return builder.build();
    }

    @Override
    public ImmutableSet<UUID> adminList() {
        ImmutableSet.Builder<UUID> builder = ImmutableSet.builder();
        adminList.forEach(v -> builder.add(UUID.fromString(v.toString())));
        return builder.build();
    }

    public boolean allows(Player player) {
        return player.isOp() || adminList.contains(player.getUniqueId());
    }

    private static String warpsToString(ImmutableMap<String, Vector> warps) {
        return warps.entrySet().stream().map(entry -> entry.getKey() + "@" + UtilWorld.vecToStrClean(entry.getValue())).collect(Collectors.joining(Constants.LOCATIONS_DELIMITER));
    }
}
