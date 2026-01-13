package colosseum.construction.command

import colosseum.construction.PermissionUtils
import org.bukkit.entity.Player

abstract class AbstractOpCommand protected constructor(
    aliases: List<String>,
    description: String,
    usage: String
) : ConstructionSiteCommand(
    aliases, description, usage
) {

    override fun canRun(caller: Player): Boolean {
        return caller.isOp || caller.hasPermission(PermissionUtils.getPermissionString(this))
    }
}
