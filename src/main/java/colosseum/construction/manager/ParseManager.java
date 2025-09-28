package colosseum.construction.manager;

import colosseum.construction.ConstructionSite;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.MapParser;
import colosseum.construction.data.FinalizedMapData;
import colosseum.utility.UtilZipper;
import colosseum.utility.WorldMapConstants;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

@ManagerDependency(WorldManager.class)
public final class ParseManager extends ConstructionSiteManager implements Runnable {
    private MapParser parser;
    private BukkitTask parserBukkitTask;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private BukkitTask selfBukkitTask;

    public ParseManager() {
        super("Parse");
    }

    private WorldManager getWorldManager() {
        return ConstructionSiteProvider.getSite().getManager(WorldManager.class);
    }

    @Override
    public void register() {
        File[] files = getWorldManager().getOnParseRootPath().listFiles();
        if (files != null) {
            for (File file : files) {
                ConstructionSiteProvider.getSite().getPluginLogger().warning("Deleting " + file.getAbsolutePath());
                FileUtils.deleteQuietly(file);
            }
        }
        selfBukkitTask = Bukkit.getScheduler().runTaskTimer(ConstructionSiteProvider.getPlugin(), this, 0, 20L);
    }

    @Override
    public void unregister() {
        this.cancel(true);
        selfBukkitTask.cancel();
        selfBukkitTask = null;
    }

    public void schedule(@NotNull World originalWorld, List<String> args, Location startPoint, int radius) {
        Bukkit.getScheduler().runTask(ConstructionSiteProvider.getPlugin(), () -> {
            originalWorld.save();
            getWorldManager().unloadWorld(originalWorld, true);
            fire(originalWorld, args, startPoint, radius);
        });
    }

    private void fire(@NotNull final World originalWorld, final List<String> args, final Location startPoint, final int radius) {
        if (isRunning()) {
            return;
        }
        try {
            running.set(true);

            WorldManager worldManager = getWorldManager();
            final File originalWorldFolder = worldManager.getWorldFolder(originalWorld);
            final String originalWorldRelativePath = worldManager.getWorldRelativePath(originalWorldFolder);

            final File destination = worldManager.getOnParseRootPath().toPath().resolve(WorldMapConstants.PARSE_PREFIX + originalWorldFolder.getName()).toFile();

            JavaPlugin plugin = ConstructionSiteProvider.getPlugin();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        worldManager.loadWorld(originalWorldRelativePath);
                        parser = new MapParser(destination, args, startPoint, radius);
                        parserBukkitTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, parser);
                    });
                } catch (Exception e) {
                    failAndCleanup(destination, e);
                }
            });
        } catch (Exception e) {
            failAndCleanup(null, e);
        }
    }

    @Override
    public void run() {
        if (parser != null) {
            final AtomicReference<MapParser.Status> status = parser.getStatus();
            for (Player player : Bukkit.getOnlinePlayers()) {
                TextComponent message = new TextComponent(String.format("A map parse task running. (%.2f%%)", getProgress() * 100.0));
                message.setColor(ChatColor.RED);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
            }
            if (status.get().isRunning()) {
                return;
            }
            final FinalizedMapData mapData = parser.mapData;
            final File worldFolder = parser.parsableWorldFolder;
            Bukkit.getScheduler().runTaskAsynchronously(ConstructionSiteProvider.getPlugin(), () -> {
                try {
                    if (status.get().isFail()) {
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
                    File zip = getWorldManager().getParsedZipOutputRootPath().toPath().resolve(worldFolder.getName() + "-" + System.currentTimeMillis() + "-" + mapData.getMapName() + "-" + mapData.getMapGameType().name() + ".zip").toFile();
                    UtilZipper.zip(worldFolder, zip);
                    ConstructionSiteProvider.getSite().getPluginLogger().info("Created " + zip.getAbsolutePath());
                    this.cancel();
                } catch (Exception e) {
                    failAndCleanup(worldFolder, e);
                }
            });
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                TextComponent message = new TextComponent("No parse task running.");
                message.setColor(ChatColor.GRAY);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
            }
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    // 0.0 - 1.0
    public double getProgress() {
        if (isRunning()) {
            return parser.getProgress();
        }
        return 0.0;
    }

    public void cancel() {
        this.cancel(false);
    }

    private void cancel(boolean unregistering) {
        if (parserBukkitTask != null) {
            parserBukkitTask.cancel();
            parserBukkitTask = null;
        }
        if (parser != null) {
            parser.getStatus().getAndUpdate((v) -> {
                if (v.isRunning()) {
                    return MapParser.Status.CANCELLED;
                }
                return v;
            });
            File folder = parser.parsableWorldFolder;
            if (unregistering) {
                FileUtils.deleteQuietly(folder);
            } else {
                Bukkit.getScheduler().runTaskLaterAsynchronously(ConstructionSiteProvider.getPlugin(), () -> FileUtils.deleteQuietly(folder), 20L);
            }
        }
        parser = null;
        running.set(false);
    }

    private void failAndCleanup(File destination, Throwable e) {
        this.cancel();
        if (destination != null) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(ConstructionSiteProvider.getPlugin(), () -> FileUtils.deleteQuietly(destination), 20L);
        }
        if (e != null) {
            ConstructionSiteProvider.getSite().getPluginLogger().log(Level.SEVERE, "Error whilst parsing map", e);
        }
    }
}
