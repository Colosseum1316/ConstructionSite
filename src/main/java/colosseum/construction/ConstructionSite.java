package colosseum.construction;

import colosseum.construction.manager.ConstructionSiteManager;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public interface ConstructionSite {
    default String getVersion() {
        return "dummy";
    }

    Logger getPluginLogger();

    File getPluginDataFolder();

    File getWorldContainer();

    Server getServer();

    FileConfiguration getConfig();

    <T extends ConstructionSiteManager> T getManager(Class<T> tClass);
}
