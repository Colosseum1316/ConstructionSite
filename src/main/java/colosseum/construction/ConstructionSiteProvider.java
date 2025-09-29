package colosseum.construction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstructionSiteProvider {
    private static ConstructionSite instance;
    private static boolean live;

    public static void setSite(ConstructionSite instance) {
        ConstructionSiteProvider.instance = instance;
    }

    public static boolean isLive() {
        return live;
    }

    public static void markLive(boolean live) {
        ConstructionSiteProvider.live = live;
    }

    public static ConstructionSite getSite() {
        return instance;
    }

    public static JavaPlugin getPlugin() {
        Validate.isTrue(instance instanceof JavaPlugin, "The provided ConstructionSite is not a JavaPlugin instance.");
        return (JavaPlugin) instance;
    }
}
