package colosseum.construction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstructionSiteProvider {
    @Nullable
    private static ConstructionSite instance;

    public static void setSite(ConstructionSite instance) {
        ConstructionSiteProvider.instance = instance;
    }

    public static ConstructionSite getSite() {
        return instance;
    }

    public static JavaPlugin getPlugin() {
        Validate.isTrue(instance instanceof JavaPlugin, "The provided ConstructionSite is not a JavaPlugin instance.");
        return (JavaPlugin) instance;
    }
}
