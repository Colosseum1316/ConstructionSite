package colosseum.construction.manager;

import colosseum.construction.CommandUtils;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.command.ConstructionSiteCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import java.util.ServiceLoader;

@ManagerDependency({MapDataManager.class, SplashTextManager.class, TeleportManager.class, WorldManager.class})
public final class CommandManager extends ConstructionSiteManager {
    public static final String FALLBACK_PREFIX = "colosseum-constructionsite";

    public CommandManager() {
        super("Command");
    }

    @Override
    public void register() {
        ServiceLoader<ConstructionSiteCommand> commandServiceLoader = ServiceLoader.load(ConstructionSiteCommand.class, ConstructionSiteCommand.class.getClassLoader());
        try {
            for (ConstructionSiteCommand c : commandServiceLoader) {
                PluginCommand command = CommandUtils.getPluginCommand(c.getAliases().get(0));
                command.setAliases(c.getAliases());
                command.setDescription(c.getDescription());
                command.setUsage(c.getUsage());
                command.setPermissionMessage(ChatColor.RED + "You can't run this command. Probably it can't be used now or you don't have permission.");
                command.setExecutor(c);
                if (c instanceof TabCompleter) {
                    command.setTabCompleter((TabCompleter) c);
                }
                ConstructionSiteProvider.getSite().getServer().getCommandMap().register(FALLBACK_PREFIX, command);
                ConstructionSiteProvider.getSite().getPluginLogger().info("Registering command " + command.getName());
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
