package colosseum.construction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstructionSiteProvider {
    private static ConstructionSite site;
    private static ConstructionSiteSchedules scheduler;
    private static boolean live;

    public static void setSite(ConstructionSite instance) {
        ConstructionSiteProvider.site = instance;
    }

    public static ConstructionSite getSite() {
        return site;
    }

    public static void setScheduler(ConstructionSiteSchedules instance) {
        ConstructionSiteProvider.scheduler = instance;
    }

    public static ConstructionSiteSchedules getScheduler() {
        return scheduler;
    }

    public static void setLive(boolean live) {
        ConstructionSiteProvider.live = live;
    }

    public static boolean isLive() {
        return live;
    }

    public static JavaPlugin getPlugin() {
        Validate.isTrue(site instanceof JavaPlugin, "The provided ConstructionSite is not a JavaPlugin instance.");
        return (JavaPlugin) site;
    }
}
