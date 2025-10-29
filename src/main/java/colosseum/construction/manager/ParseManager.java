package colosseum.construction.manager;

import colosseum.construction.ConstructionSite;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.WorldUtils;
import colosseum.construction.data.FinalizedMapData;
import colosseum.construction.parser.MapParser;
import colosseum.utility.UtilZipper;
import colosseum.utility.WorldMapConstants;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public final class ParseManager extends ConstructionSiteManager {
    private volatile MapParser parser;
    private BukkitTask parserBukkitTask;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private BukkitTask selfBukkitTask;

    public ParseManager() {
        super("Parse");
    }

    @Override
    public void register() {
        File[] files = WorldUtils.getOnParseRootPath().listFiles();
        if (files != null) {
            for (File file : files) {
                ConstructionSiteProvider.getSite().getPluginLogger().warning("Deleting " + file.getAbsolutePath());
                FileUtils.deleteQuietly(file);
            }
        }
        selfBukkitTask = ConstructionSiteProvider.getScheduler().schedule(this::query, BukkitTask.class, 0L, 20L);
    }

    @Override
    public void unregister() {
        this.cancel0(true);
        selfBukkitTask.cancel();
        selfBukkitTask = null;
    }

    public void schedule(@NotNull final World originalWorld, final List<String> args, final Location startPoint, final int radius) {
        ConstructionSiteProvider.getScheduler().schedule(() -> {
            if (isRunning()) {
                ConstructionSiteProvider.getSite().getPluginLogger().warning("A map parse task is running. Double check your invocation...");
                return;
            }
            originalWorld.save();
            try {
                WorldUtils.unloadWorld(originalWorld, true);
            } catch (Exception e) {
                ConstructionSiteProvider.getSite().getPluginLogger().log(Level.SEVERE, "Cannot unload world for parsing!", e);
                return;
            }
            try {
                running.set(true);

                final File originalWorldFolder = WorldUtils.getWorldFolder(originalWorld);
                final String originalWorldRelativePath = WorldUtils.getWorldRelativePath(originalWorldFolder);
                final File destination = WorldUtils.getOnParseRootPath().toPath().resolve(WorldMapConstants.PARSE_PREFIX + originalWorldFolder.getName()).toFile();

                ConstructionSiteProvider.getScheduler().scheduleAsync(() -> {
                    try {
                        if (destination.exists()) {
                            FileUtils.deleteDirectory(destination);
                        }

                        ConstructionSite site = ConstructionSiteProvider.getSite();
                        site.getPluginLogger().info("Preparing world parse. Copying " + originalWorldFolder.getAbsolutePath() + " to " + destination.getAbsolutePath());
                        FileUtils.copyDirectory(originalWorldFolder, destination);

                        site.getPluginLogger().info("Deleting unneeded files in " + destination.getAbsolutePath());
                        for (File file : Objects.requireNonNull(destination.listFiles())) {
                            String filename = file.getName();
                            if (!filename.equalsIgnoreCase(WorldMapConstants.LEVEL_DAT)
                                    && !filename.equalsIgnoreCase(WorldMapConstants.REGION)
                                    && !filename.equalsIgnoreCase(WorldMapConstants.WORLDCONFIG_DAT)
                                    && !filename.equalsIgnoreCase(WorldMapConstants.MAP_DAT)) {
                                FileUtils.deleteQuietly(file);
                            }
                        }

                        final FinalizedMapData mapData = site.getManager(MapDataManager.class).getFinalized(destination);
                        ConstructionSiteProvider.getScheduler().schedule(() -> {
                            WorldUtils.loadWorld(originalWorldRelativePath);
                            parser = new MapParser(destination, mapData, args, startPoint.getBlockX(), startPoint.getBlockZ(), radius);
                            parserBukkitTask = ConstructionSiteProvider.getScheduler().scheduleAsync(parser, BukkitTask.class);
                        });
                    } catch (Exception e) {
                        failAndCleanup(destination, e);
                    }
                });
            } catch (Exception e) {
                failAndCleanup(null, e);
            }
        });
    }

    private void query() {
        if (parser != null) {
            for (Player player : ConstructionSiteProvider.getSite().getServer().getOnlinePlayers()) {
                TextComponent message = new TextComponent(String.format("A map parse task running. (%.2f%%)", getProgress() * 100.0));
                message.setColor(ChatColor.RED);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
            }
            if (parser.isRunning()) {
                return;
            }
            final FinalizedMapData mapData = parser.mapData;
            final File worldFolder = parser.parsableWorldFolder;
            ConstructionSiteProvider.getScheduler().scheduleAsync(() -> {
                try {
                    if (parser.isFail()) {
                        failAndCleanup(worldFolder, null);
                        return;
                    }
                    for (File file : Objects.requireNonNull(worldFolder.listFiles())) {
                        String filename = file.getName();
                        if (!filename.equalsIgnoreCase(WorldMapConstants.LEVEL_DAT)
                                && !filename.equalsIgnoreCase(WorldMapConstants.REGION)
                                && !filename.equalsIgnoreCase(WorldMapConstants.WORLDCONFIG_DAT)) {
                            FileUtils.deleteQuietly(file);
                        }
                    }
                    File zip = WorldUtils.getParsedZipOutputRootPath().toPath().resolve(worldFolder.getName() + "-" + System.currentTimeMillis() + "-" + mapData.getMapName().get() + "-" + mapData.getMapGameType().get().name() + ".zip").toFile();
                    UtilZipper.zip(worldFolder, zip);
                    ConstructionSiteProvider.getSite().getPluginLogger().info("Created " + zip.getAbsolutePath());
                    this.cancel();
                } catch (Exception e) {
                    failAndCleanup(worldFolder, e);
                }
            });
        } else {
            for (Player player : ConstructionSiteProvider.getSite().getServer().getOnlinePlayers()) {
                TextComponent message = new TextComponent("No parse task running.");
                message.setColor(ChatColor.GRAY);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
            }
        }
    }

    public void cancel() {
        this.cancel0(false);
    }

    public boolean isRunning() {
        return running.get();
    }

    /** 0.0 - 1.0 */
    public double getProgress() {
        if (isRunning()) {
            return parser != null ? parser.getProgress() : 0.0;
        }
        return 0.0;
    }

    private void cancel0(boolean unregistering) {
        if (parserBukkitTask != null) {
            parserBukkitTask.cancel();
            parserBukkitTask = null;
        }
        if (parser != null) {
            parser.cancel();
            File folder = parser.parsableWorldFolder;
            if (unregistering) {
                FileUtils.deleteQuietly(folder);
            } else {
                ConstructionSiteProvider.getScheduler().scheduleAsync(() -> {
                    FileUtils.deleteQuietly(folder);
                });
            }
        }
        parser = null;
        running.set(false);
    }

    private void failAndCleanup(File destination, Throwable e) {
        this.cancel();
        if (destination != null) {
            ConstructionSiteProvider.getScheduler().scheduleAsync(() -> {
                FileUtils.deleteQuietly(destination);
            });
        }
        if (e != null) {
            ConstructionSiteProvider.getSite().getPluginLogger().log(Level.SEVERE, "Error whilst running scheduled task", e);
        }
    }
}
