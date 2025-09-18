package colosseum.construction.command.vanilla

import colosseum.construction.command.AbstractMapAdminCommand
import org.bukkit.command.Command
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
            Command.broadcastCommandMessage(caller, "Set map ${getMapDataManager().get(caller.world).mapName} world time to $t", true)
        } catch (e: NumberFormatException) {
            return false
        }
        return true
    }
}