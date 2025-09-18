package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.utility.UtilPlayerBase
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent

class OpPrivateMessageCommand: AbstractOpCommand(
    listOf("opdm", "oppm"),
    "Private message to all ops.",
    "/opdm <message>"
) {
    override fun canRun(console: CommandSender): Boolean {
        return true
    }

    override fun runConstruction(console: CommandSender, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            UtilPlayerBase.sendMessage(console, "&cWhere's your tongue bro?")
            return false
        }
        val recipients = Bukkit.getServer().onlinePlayers.filter { it.isOp }.toList()
        if (recipients.isEmpty()) {
            UtilPlayerBase.sendMessage(console, "&7There's nobody...")
            return true
        }
        val message = args.joinToString(" ")
        Bukkit.getScheduler().runTaskAsynchronously(ConstructionSiteProvider.getPlugin()) {
            for (recipient in recipients) {
                UtilPlayerBase.sendMessage(recipient, "&7Console: &r$message")
            }
        }
        UtilPlayerBase.sendMessage(console, "&7Console: &r$message")
        return true
    }

    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            UtilPlayerBase.sendMessage(caller, "&cWhere's your tongue bro?")
            return false
        }
        val recipients = Bukkit.getServer().onlinePlayers.filter { it.isOp }.toHashSet()
        Bukkit.getScheduler().runTaskAsynchronously(ConstructionSiteProvider.getPlugin()) {
            val chatEvent = AsyncPlayerChatEvent(true, caller, args.joinToString(" "), recipients)
            Bukkit.getPluginManager().callEvent(chatEvent)
            if (chatEvent.isCancelled) {
                return@runTaskAsynchronously
            }
            val s = String.format(chatEvent.format, chatEvent.player.name, chatEvent.message)
            Bukkit.getServer().consoleSender.sendMessage("OP $s")
            for (recipient in recipients) {
                UtilPlayerBase.sendMessage(recipient, "&f&lOP&r $s")
            }
        }
        return true
    }
}
