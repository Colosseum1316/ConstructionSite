package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.data.MutableMapData
import colosseum.utility.UtilPlayerBase
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.entity.Player

class MapCurrentlyLiveCommand: AbstractMapAdminCommand(
    listOf("mapislive", "mapsetlive"),
    "Get/Set map live status.",
    """
        /mapislive
        /mapsetlive
    """.trimIndent()
) {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        val data = getMapDataManager().get(caller.world) as MutableMapData
        if (label.equals("mapsetlive", ignoreCase = true)) {
            data.isCurrentlyLive = !data.isCurrentlyLive
            Bukkit.getScheduler().runTaskAsynchronously(ConstructionSiteProvider.getPlugin()) {
                data.write()
            }
            Command.broadcastCommandMessage(caller, "${data.mapName} is ${if (data.isCurrentlyLive) "now live" else "no longer live"}", true)
        } else {
            UtilPlayerBase.sendMessage(caller, "&e${data.mapName}&r is ${if (data.isCurrentlyLive) "&alive" else "&cnot live"}")
        }
        return true
    }
}
