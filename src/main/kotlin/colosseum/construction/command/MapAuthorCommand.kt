package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MutableMapData
import org.bukkit.command.Command
import org.bukkit.entity.Player
import java.util.function.*

class MapAuthorCommand: AbstractMapCreditCommand(
    listOf("mapauthor", "mapauthors"),
    "Set map author data.",
    "/mapauthor <Map author(s)>"
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
        val newMapCreator = contentSupplier.get()
        ConstructionSiteProvider.getScheduler().scheduleAsync {
            data.update(FinalizedMapData(null, newMapCreator))
            Command.broadcastCommandMessage(caller, "Map ${mapDataSupplier.get().mapName} set author: ${mapDataSupplier.get().mapCreator}")
            ConstructionSiteProvider.getSite().pluginLogger.info("World $path set author: ${mapDataSupplier.get().mapCreator}")
        }
        return true
    }
}
