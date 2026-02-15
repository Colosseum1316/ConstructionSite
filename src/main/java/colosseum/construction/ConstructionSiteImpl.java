package colosseum.construction;

import colosseum.construction.manager.ConstructionSiteManager;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "unchecked"})
public final class ConstructionSiteImpl extends JavaPlugin implements ConstructionSite {

    private ServiceLoader<ConstructionSiteManager> managerProviders;
    private final Map<Class<? extends ConstructionSiteManager>, ConstructionSiteManager> managers = new HashMap<>();
    private final ArrayList<Class<? extends ConstructionSiteManager>> managerClassReference = new ArrayList<>();

    @Override
    public <T extends ConstructionSiteManager> T getManager(Class<T> tClass) {
        return (T) managers.get(tClass);
    }

    @Override
    public void onLoad() {
        getLogger().info(String.format("Plugin version: %s (%s)", getDescription().getVersion(), getVersion()));
        Validate.isTrue(BaseUtils.initDir(getPluginDataFolder()), "Cannot initialize plugin data folder");
        ConstructionSiteProvider.setSite(this);
        if (managerProviders == null) {
            managerProviders = ServiceLoader.load(ConstructionSiteManager.class, ConstructionSiteManager.class.getClassLoader());
        }
        PluginUtils.deleteWorlds();
        try {
            PluginUtils.unzip();
            managers.clear();
            managerClassReference.clear();
            managerClassReference.addAll(PluginUtils.discoverManagers(managerProviders));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void onEnable() {
        ConstructionSiteProvider.setScheduler(new ConstructionSiteSchedulesImpl());
        for (ConstructionSiteManager provider : managerProviders) {
            managers.put(provider.getClass(), provider);
        }
        PluginUtils.registerManagers(managerClassReference, managers);
        ConstructionSiteProvider.setLive(true);
    }

    @Override
    public void onDisable() {
        ConstructionSiteProvider.setLive(false);
        PluginUtils.unregisterManagers(managerClassReference, managers);
        ConstructionSiteProvider.setScheduler(null);
        managers.clear();
        managerClassReference.clear();
        ConstructionSiteProvider.setSite(null);
        managerProviders = null;
    }

    @Override
    public Logger getPluginLogger() {
        return super.getLogger();
    }

    @Override
    public File getPluginDataFolder() {
        return super.getDataFolder();
    }

    @Override
    public File getWorldContainer() {
        return this.getServer().getWorldContainer();
    }

    @Override
    public String getVersion() {
        return RevConstants.BUILD_VERSION;
    }
}
