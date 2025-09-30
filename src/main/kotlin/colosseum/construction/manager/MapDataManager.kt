package colosseum.construction.manager

import colosseum.construction.BaseUtils
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.data.DummyMapData
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MapDataImpl
import colosseum.construction.data.MutableMapData
import colosseum.utility.MapData
import com.google.common.collect.Maps
import org.bukkit.World
import java.io.File
import java.util.concurrent.*
import java.util.function.*

@ManagerDependency(WorldManager::class)
class MapDataManager: ConstructionSiteManager("MapData") {
    private val mapData: ConcurrentMap<String, MapData> = Maps.newConcurrentMap()

    companion object {
        private fun getWorldManager(): WorldManager {
            return ConstructionSiteProvider.getSite().getManager(WorldManager::class.java)
        }

        private var getMapRootDir = Function<World, File> { world ->
            getWorldManager().getWorldFolder(world)
        }
    }

    override fun register() {
        getWorldManager().also {
            it.getMapsRootPath().listFiles()?.forEach { f ->
                if (f.isDirectory) {
                    val path = it.getWorldRelativePath(f)
                    it.loadWorld(path)?.let { world -> get(world) }
                }
            }
        }
    }

    override fun unregister() {
        mapData.entries.removeAll { entry ->
            if (entry.value is MutableMapData) {
                (entry.value as MutableMapData).save()
            }
            return@removeAll true
        }
    }

    fun get(world: World): MapData {
        getMapRootDir.apply(world).also { dir ->
            return mapData.computeIfAbsent(dir.absolutePath) {
                return@computeIfAbsent if (BaseUtils.isLevelNamePreserved(getWorldManager().getWorldRelativePath(world))) {
                    DummyMapData(world, dir)
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
