package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MutableMapData
import colosseum.utility.UtilPlayerBase
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
            ConstructionSiteProvider.getSchedules().scheduleAsync({
                data.updateAndWrite(FinalizedMapData(null, null, null, !data.isLive))
                Command.broadcastCommandMessage(caller, "${data.mapName} is ${if (data.isLive) "now live" else "no longer live"}", true)
            }, Void::class.java)
        } else {
            UtilPlayerBase.sendMessage(caller, "&e${data.mapName}&r is ${if (data.isLive) "&alive" else "&cnot live"}")
        }
        return true
    }
}
