package colosseum.construction.command;

import colosseum.construction.ConstructionSiteProvider;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class ConstructionSiteCommand implements CommandExecutor {
    private final List<String> aliases = new ArrayList<>();
    private final String description;
    private final String usage;

    protected ConstructionSiteCommand(String name) {
        this(Arrays.asList(name));
    }

    protected ConstructionSiteCommand(String name, String usage) {
        this(Arrays.asList(name), usage);
    }

    protected ConstructionSiteCommand(String name, String description, String usage) {
        this(Arrays.asList(name), description, usage);
    }

    protected ConstructionSiteCommand(List<String> aliases) {
        this(aliases, "/" + aliases.get(0));
    }

    protected ConstructionSiteCommand(List<String> aliases, String usage) {
        this(aliases, "", usage);
    }

    protected ConstructionSiteCommand(List<String> aliases, String description, String usage) {
        this.aliases.addAll(aliases);
        this.description = description;
        this.usage = usage;
    }

    public boolean canRun(CommandSender console) {
        return false;
    }

    public boolean canRun(Player caller) {
        return ConstructionSiteProvider.isLive();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if ((sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) && canRun((CommandSender) sender)) {
                return runConstruction(sender, label, args);
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player.");
                return true;
            }
        }
        if (!canRun((Player) sender)) {
            sender.sendMessage(ChatColor.RED + "You can't run this command. Probably it can't be used now or you don't have permission.");
            return true;
        } else {
            return runConstruction((Player) sender, label, args);
        }
    }

    public boolean runConstruction(CommandSender sender, String label, String[] args) {
        return true;
    }

    public boolean runConstruction(Player caller, String label, String[] args) {
        return true;
    }
}
