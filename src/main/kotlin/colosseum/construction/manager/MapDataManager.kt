package colosseum.construction.manager

import colosseum.construction.WorldUtils
import colosseum.construction.data.DummyMapData
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MapData
import colosseum.construction.data.MapDataImpl
import colosseum.construction.data.MutableMapData
import com.google.common.collect.Maps
import org.bukkit.World
import java.io.File
import java.util.concurrent.*
import java.util.function.Function

@ManagerDependency(WorldManager::class)
class MapDataManager : ConstructionSiteManager("MapData") {
    private val mapData: ConcurrentMap<String, MapData> = Maps.newConcurrentMap()

    companion object {
        private var getMapRootDir = Function<World, File> { world ->
            WorldUtils.getWorldFolder(world)
        }
    }

    override fun register() {
        WorldUtils.getMapsRootPath().listFiles()?.forEach { f ->
            if (f.isDirectory) {
                val path = WorldUtils.getWorldRelativePath(f)
                WorldUtils.loadWorld(path)?.let { world -> get(world) }
            }
        }
    }

    override fun unregister() {
        mapData.entries.removeAll { entry ->
            if (entry.value is MutableMapData) {
                (entry.value as MutableMapData).write()
            }
            return@removeAll true
        }
    }

    fun get(world: World): MapData {
        getMapRootDir.apply(world).also { dir ->
            return mapData.computeIfAbsent(dir.absolutePath) {
                return@computeIfAbsent if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(world))) {
                    DummyMapData()
                } else {
                    MapDataImpl(world, dir)
                }
            }
        }
    }

    fun getFinalized(worldFolder: File): FinalizedMapData {
        return FinalizedMapData(MapDataImpl(null, worldFolder))
    }

    fun discard(world: World?) {
        if (world != null) {
            mapData.remove(getMapRootDir.apply(world).absolutePath)
        }
    }
}
