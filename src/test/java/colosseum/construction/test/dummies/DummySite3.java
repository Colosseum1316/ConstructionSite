package colosseum.construction.test.dummies;

import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.PluginUtils;
import colosseum.construction.manager.ConstructionSiteManager;
import colosseum.construction.manager.MapDataManager;
import colosseum.construction.manager.TeleportManager;
import colosseum.construction.manager.WorldManager;
import colosseum.construction.test.Utils;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class DummySite3 implements DummySite {
    private final Logger logger;

    private final Map<Class<? extends ConstructionSiteManager>, ConstructionSiteManager> managers = new HashMap<>();
    private final ArrayList<Class<? extends ConstructionSiteManager>> managersReference = new ArrayList<>();

    private final File worldContainer;
    private final File pluginDataFolder;

    private final ConstructionSiteServerMock serverMock;

    public DummySite3(File worldContainer, File pluginDataFolder) {
        this.worldContainer = worldContainer;
        this.pluginDataFolder = pluginDataFolder;

        this.logger = Utils.getSiteLogger();

        managers.clear();
        managersReference.clear();
        managersReference.addAll(PluginUtils.discoverManagers(Arrays.asList(WorldManager.class, MapDataManager.class, TeleportManager.class)));

        serverMock = ConstructionSiteServerMock.mock();
    }

    @Override
    public <T extends ConstructionSiteManager> T getManager(Class<T> tClass) {
        return (T) managers.get(tClass);
    }

    @Override
    public void load() {
        ConstructionSiteProvider.setSite(this);
    }

    @Override
    public void enable() {
        ConstructionSiteProvider.setScheduler(new DummySchedules());
        managers.put(WorldManager.class, new WorldManager());
        managers.put(MapDataManager.class, new MapDataManager());
        managers.put(TeleportManager.class, new TeleportManager());
        PluginUtils.registerManagers(managersReference, managers);
        ConstructionSiteProvider.setLive(true);
    }

    @Override
    public void disable() {
        ConstructionSiteProvider.setLive(false);
        PluginUtils.unregisterManagers(managersReference, managers);
        ConstructionSiteProvider.setScheduler(null);
        managers.clear();
        managersReference.clear();
        ConstructionSiteProvider.setSite(null);
    }

    @Override
    public Logger getPluginLogger() {
        return logger;
    }

    @Override
    public File getPluginDataFolder() {
        return pluginDataFolder;
    }

    @Override
    public File getWorldContainer() {
        return worldContainer;
    }

    @Override
    public Server getServer() {
        return serverMock;
    }

    @Override
    public FileConfiguration getConfig() {
        return null;
    }
}
