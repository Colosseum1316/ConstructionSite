package colosseum.construction.manager;

import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.command.ConstructionSiteCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.ServiceLoader;

@ManagerDependency({GameTypeInfoManager.class, MapDataManager.class, SplashTextManager.class, TeleportManager.class, WorldManager.class})
public final class CommandManager extends ConstructionSiteManager {
    public static final String FALLBACK_PREFIX = "colosseum-constructionsite";

    public CommandManager() {
        super("Command");
    }

    private static final Constructor<PluginCommand> pluginCommandConstructor;
    static {
        try {
            pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
        pluginCommandConstructor.setAccessible(true);
    }

    private static PluginCommand getPluginCommand(String label) {
        PluginCommand command;
        try {
            command = pluginCommandConstructor.newInstance(label, ConstructionSiteProvider.getPlugin());
        } catch (Exception e) {
            throw new Error(e);
        }
        return command;
    }

    @Override
    public void register() {
        ServiceLoader<ConstructionSiteCommand> commandServiceLoader = ServiceLoader.load(ConstructionSiteCommand.class, ConstructionSiteCommand.class.getClassLoader());
        try {
            for (ConstructionSiteCommand provider : commandServiceLoader) {
                Class<? extends ConstructionSiteCommand> providerClass = provider.getClass().asSubclass(ConstructionSiteCommand.class);
                ConstructionSiteCommand c = providerClass.getDeclaredConstructor().newInstance();
                PluginCommand command = getPluginCommand(c.getAliases().get(0));
                command.setAliases(c.getAliases());
                command.setDescription(c.getDescription());
                command.setUsage(c.getUsage());
                command.setPermissionMessage(ChatColor.RED + "You can't run this command. Probably it can't be used now or you don't have permission.");
                command.setExecutor(c);
                if (c instanceof TabCompleter) {
                    command.setTabCompleter((TabCompleter) c);
                }
                Bukkit.getServer().getCommandMap().register(FALLBACK_PREFIX, command);
                ConstructionSiteProvider.getSite().getPluginLogger().info("Registering command " + command.getName());
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
