package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.manager.SplashTextManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class OpAddSplashTextCommand: AbstractOpCommand(
    listOf("addtext"),
    "Add splash text.",
    "/addtext <text>"
) {
    override fun canRun(console: CommandSender): Boolean {
        return true
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        return runConstruction(caller as CommandSender, label, args)
    }

    override fun runConstruction(caller: CommandSender, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            return false
        }
        val content = args.joinToString(" ").trim { it <= ' ' }
        ConstructionSiteProvider.getSite().getManager(SplashTextManager::class.java).addText(content)
        Command.broadcastCommandMessage(caller, "Add splash text: $content", true)
        return true
    }
}
