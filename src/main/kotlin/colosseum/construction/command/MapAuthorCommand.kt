package colosseum.construction.command

import colosseum.construction.ConstructionSiteProvider
import colosseum.utility.MutableMapData
import org.bukkit.Bukkit
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
    ) {
        mapDataSupplier.get().mapCreator = contentSupplier.get()
    }

    override fun postAction(
        caller: Player,
        alias: String,
        args: Array<String>,
        contentSupplier: Supplier<String>,
        mapDataSupplier: Supplier<MutableMapData>
    ): Boolean {
        val world = caller.world
        val worldManager = getWorldManager()
        val path = worldManager.getWorldRelativePath(world)
        Bukkit.getScheduler().runTaskAsynchronously(ConstructionSiteProvider.getPlugin()) {
            mapDataSupplier.get().write()
        }
        Command.broadcastCommandMessage(caller, "Map ${mapDataSupplier.get().mapName} set author: ${mapDataSupplier.get().mapCreator}")
        ConstructionSiteProvider.getSite().pluginLogger.info("World $path set author: ${mapDataSupplier.get().mapCreator}")
        return true
    }
}
