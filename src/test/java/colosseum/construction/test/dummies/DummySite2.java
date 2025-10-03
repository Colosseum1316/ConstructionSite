package colosseum.construction.test.dummies;

import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.manager.ConstructionSiteManager;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.logging.Logger;

@SuppressWarnings("ClassCanBeRecord")
public final class DummySite2 implements DummySite {

    private final File worldContainer;

    public DummySite2(File worldContainer) {
        this.worldContainer = worldContainer;
    }

    @Override
    public <T extends ConstructionSiteManager> T getManager(Class<T> tClass) {
        return null;
    }

    @Override
    public void setup() {
        ConstructionSiteProvider.setSite(this);
        ConstructionSiteProvider.setLive(true);
    }

    @Override
    public void teardown() {
        ConstructionSiteProvider.setLive(false);
        ConstructionSiteProvider.setSite(null);
    }

    @Override
    public Logger getPluginLogger() {
        return null;
    }

    @Override
    public File getPluginDataFolder() {
        return null;
    }

    @Override
    public File getWorldContainer() {
        return worldContainer;
    }

    @Override
    public Server getServer() {
        return null;
    }

    @Override
    public FileConfiguration getConfig() {
        return null;
    }
}
