package colosseum.construction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandUtils {
    private static final Constructor<PluginCommand> pluginCommandConstructor;

    static {
        try {
            pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
        pluginCommandConstructor.setAccessible(true);
    }

    public static PluginCommand getPluginCommand(String label) {
        PluginCommand command;
        try {
            command = pluginCommandConstructor.newInstance(label, ConstructionSiteProvider.getPlugin());
        } catch (Exception e) {
            throw new Error(e);
        }
        return command;
    }
}
