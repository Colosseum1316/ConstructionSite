package colosseum.construction;

import colosseum.construction.manager.ConstructionSiteManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class ConstructionSiteImpl extends JavaPlugin implements ConstructionSite {

    private final Map<Class<? extends ConstructionSiteManager>, ConstructionSiteManager> managers = new HashMap<>();
    private final ArrayList<Class<? extends ConstructionSiteManager>> managersReference = new ArrayList<>();

    @Override
    public <T extends ConstructionSiteManager> T getManager(Class<T> tClass) {
        return (T) managers.get(tClass);
    }

    @Override
    public void onLoad() {
        Validate.isTrue(BaseUtils.initDir(getPluginDataFolder()), "Cannot initialize plugin data folder");
        ConstructionSiteProvider.setSite(this);
        try {
            PluginUtils.unzip();
            managers.clear();
            managersReference.clear();
            managersReference.addAll(PluginUtils.discoverManagers());
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public void onEnable() {
        ConstructionSiteProvider.setScheduler(new ConstructionSiteSchedulesImpl());
        PluginUtils.registerManagers(managersReference, managers);
        ConstructionSiteProvider.setLive(true);
    }

    @Override
    public void onDisable() {
        ConstructionSiteProvider.setLive(false);
        PluginUtils.unregisterManagers(managersReference, managers);
        ConstructionSiteProvider.setScheduler(null);
        managers.clear();
        managersReference.clear();
        ConstructionSiteProvider.setSite(null);
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
        return Bukkit.getWorldContainer();
    }
}
