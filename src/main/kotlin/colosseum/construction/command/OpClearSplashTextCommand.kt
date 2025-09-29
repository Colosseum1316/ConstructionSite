package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.manager.SplashTextManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class OpClearSplashTextCommand: AbstractOpCommand(
    listOf("cleartext"),
    "Clear all splash text.",
    "/cleartext"
) {
    override fun canRun(console: CommandSender): Boolean {
        return ConstructionSiteProvider.isLive()
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        return runConstruction(caller as CommandSender, label, args)
    }

    override fun runConstruction(caller: CommandSender, label: String, args: Array<String>): Boolean {
        ConstructionSiteProvider.getSite().getManager(SplashTextManager::class.java).clearText()
        Command.broadcastCommandMessage(caller, "Clear all splash text.", true)
        return true
    }
}
