package colosseum.construction.manager;

import colosseum.construction.CommandUtils;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.PermissionUtils;
import colosseum.construction.command.ConstructionSiteCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.permissions.Permission;

import java.util.ServiceLoader;

@ManagerDependency({MapDataManager.class, TeleportManager.class, WorldManager.class})
public final class CommandManager extends ConstructionSiteManager {
    public static final String FALLBACK_PREFIX = "colosseum-constructionsite";

    public CommandManager() {
        super("Command");
    }

    private ServiceLoader<ConstructionSiteCommand> commandProviders;

    @Override
    public void register() {
        commandProviders = ServiceLoader.load(ConstructionSiteCommand.class, ConstructionSiteCommand.class.getClassLoader());
        try {
            for (ConstructionSiteCommand c : commandProviders) {
                String alias = c.getAliases().get(0);
                PluginCommand command = CommandUtils.getPluginCommand(alias);
                command.setAliases(c.getAliases());
                command.setDescription(c.getDescription());
                command.setUsage(c.getUsage());
                command.setPermissionMessage(ChatColor.RED + "You can't run this command. Probably it can't be used now or you don't have permission.");
                command.setExecutor(c);
                if (c instanceof TabCompleter) {
                    command.setTabCompleter((TabCompleter) c);
                }
                command.setPermission(String.format("%s;%s", PermissionUtils.getPermissionString(c), PermissionUtils.getAsteriskPermissionString()));
                CommandUtils.getCommandMap().register(FALLBACK_PREFIX, command);
                ConstructionSiteProvider.getSite().getPluginLogger().info("Registering command " + command.getName());
            }
        } catch (Exception e) {
            throw new Error(e);
        }
        Permission asterisk = PermissionUtils.getAsteriskPermission();
        ConstructionSiteProvider.getSite().getServer().getPluginManager().addPermission(asterisk);
        for (ConstructionSiteCommand c : commandProviders) {
            Permission cp = PermissionUtils.getPermission(c);
            cp.addParent(asterisk, true);
            ConstructionSiteProvider.getSite().getServer().getPluginManager().addPermission(cp);
        }
    }

    @Override
    public void unregister() {
        if (commandProviders == null) {
            return;
        }
        for (ConstructionSiteCommand c : commandProviders) {
            ConstructionSiteProvider.getSite().getServer().getPluginManager().removePermission(PermissionUtils.getPermission(c));
        }
        ConstructionSiteProvider.getSite().getServer().getPluginManager().removePermission(PermissionUtils.getAsteriskPermission());
        commandProviders = null;
    }
}
