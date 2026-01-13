package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MutableMapData
import colosseum.utility.UtilPlayerBase
import org.bukkit.entity.Player
import java.util.function.*

class MapNameCommand : AbstractMapCreditCommand(
    listOf("mapname"),
    "Set map name.",
    "/mapname <Map name>"
) {
    override fun determineContent(caller: Player, alias: String, args: Array<String>): String {
        return content(args).toString()
    }

    override fun setField(
        caller: Player,
        alias: String,
        args: Array<String>,
        contentSupplier: Supplier<String>,
        mapDataSupplier: Supplier<MutableMapData>
    ): Boolean {
        val world = caller.world
        val path = WorldUtils.getWorldRelativePath(world)
        val data = mapDataSupplier.get()
        val newMapName = contentSupplier.get()
        ConstructionSiteProvider.getScheduler().scheduleAsync {
            data.update(FinalizedMapData(newMapName, null))
            UtilPlayerBase.sendMessage(caller, "Set map name: $newMapName")
            ConstructionSiteProvider.getSite().pluginLogger.info("World $path set map name: $newMapName")
        }
        return true
    }
}
