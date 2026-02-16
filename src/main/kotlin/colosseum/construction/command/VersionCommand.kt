package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.utility.UtilPlayerBase
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class VersionCommand : AbstractOpCommand(
    listOf("csversion", "csver"),
    "Get plugin version",
    """
        /csversion
    """.trimIndent()
) {
    override fun canRun(console: CommandSender): Boolean {
        return ConstructionSiteProvider.isLive()
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        return runConstruction(caller as CommandSender, label, args)
    }

    override fun runConstruction(caller: CommandSender, label: String, args: Array<String>): Boolean {
        UtilPlayerBase.sendMessage(caller, "Plugin version: ${ConstructionSiteProvider.getPlugin().description.version}")
        UtilPlayerBase.sendMessage(caller, "Build version: ${ConstructionSiteProvider.getSite().version}")
        return true
    }
}