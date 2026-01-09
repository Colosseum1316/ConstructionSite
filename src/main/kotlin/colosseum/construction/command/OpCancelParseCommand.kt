package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.manager.ParseManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class OpCancelParseCommand : AbstractOpCommand(
    listOf("cancelparse"),
    "Cancel running parse task.",
    "/cancelparse"
) {
    override fun canRun(console: CommandSender): Boolean {
        return ConstructionSiteProvider.isLive()
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        return runConstruction(caller as CommandSender, label, args)
    }

    override fun runConstruction(caller: CommandSender, label: String, args: Array<String>): Boolean {
        val server = ConstructionSiteProvider.getSite().getServer()
        server.dispatchCommand(server.consoleSender, "mapparse")
        ConstructionSiteProvider.getSite().getManager(ParseManager::class.java).cancel()
        server.dispatchCommand(server.consoleSender, "mapparse")
        return true
    }
}