package colosseum.construction.command.vanilla

import colosseum.construction.command.AbstractMapAdminCommand
import colosseum.utility.UtilPlayerBase
import org.bukkit.entity.Player

class TimeCommand: AbstractMapAdminCommand(
    listOf("time"),
    "Set map world time",
    "/time <absolute tick>"
) {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.size != 1) {
            return false
        }

        try {
            val t = args[0].toLong()
            if (t < 0) {
                throw NumberFormatException()
            }
            caller.world.time = t
            UtilPlayerBase.sendMessage(caller, "Set map world time to $t")
        } catch (_: NumberFormatException) {
            return false
        }
        return true
    }
}