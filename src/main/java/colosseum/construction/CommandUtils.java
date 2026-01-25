package colosseum.construction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandUtils {
    private static final Constructor<PluginCommand> pluginCommandConstructor;
    private static final Field commandMapField;
    static {
        try {
            pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new Error(e);
        }
        pluginCommandConstructor.setAccessible(true);
        commandMapField.setAccessible(true);
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

    public static CommandMap getCommandMap() {
        try {
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
