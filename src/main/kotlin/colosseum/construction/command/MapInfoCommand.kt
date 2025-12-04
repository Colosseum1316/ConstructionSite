package colosseum.construction.command

import colosseum.utility.UtilPlayerBase
import org.bukkit.entity.Player

class MapInfoCommand: AbstractMapAdminCommand(listOf("mapinfo"), "Get map info.", "/mapinfo") {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        val data = getMapDataManager().get(caller.world)
        UtilPlayerBase.sendMessage(caller, "Map name: &e${data.mapName}")
        UtilPlayerBase.sendMessage(caller, "Author: &e${data.mapCreator}")
        return true
    }
}
