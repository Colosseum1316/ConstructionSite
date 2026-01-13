package colosseum.construction.command

import colosseum.utility.UtilPlayerBase
import org.bukkit.Material
import org.bukkit.entity.Player

abstract class ItemCommand protected constructor(
    aliases: List<String>,
    description: String,
    usage: String
) : ConstructionSiteCommand(
    aliases, description, usage
) {
    override fun canRun(caller: Player): Boolean {
        val item = caller.itemInHand
        if (item == null || item.type.equals(Material.AIR)) {
            UtilPlayerBase.sendMessage(caller, "&cHold an item in your hand!")
            return false
        }
        return true
    }

    protected fun content(args: Array<String>): StringBuilder {
        return StringBuilder(args.joinToString(" ").replace("&".toRegex(), "§").trim { it <= ' ' })
    }

    protected fun determineContent(args: Array<String>): String {
        return content(args).toString()
    }
}
