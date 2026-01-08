package colosseum.construction

import colosseum.construction.command.AbstractOpCommand
import colosseum.construction.command.ConstructionSiteCommand
import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

object PermissionUtils {
    @JvmStatic
    fun getPermissionString(command: ConstructionSiteCommand): String {
        return "colosseum.construction.${command.aliases[0]}"
    }

    @JvmStatic
    fun getAsteriskPermissionString(): String {
        return "colosseum.construction.*"
    }

    @JvmStatic
    fun getAsteriskPermission(): Permission {
        return Bukkit.getPluginManager().getPermission(getAsteriskPermissionString()) ?: Permission(getAsteriskPermissionString(), PermissionDefault.OP)
    }

    @JvmStatic
    fun getPermission(command: ConstructionSiteCommand): Permission {
        return Bukkit.getPluginManager().getPermission(getPermissionString(command)) ?: Permission(getPermissionString(command), if (command is AbstractOpCommand) PermissionDefault.OP else PermissionDefault.TRUE)
    }
}