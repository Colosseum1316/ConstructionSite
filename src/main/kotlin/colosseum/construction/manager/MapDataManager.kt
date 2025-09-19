package colosseum.construction.manager

import colosseum.construction.BaseUtils
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.data.DummyMapData
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MapData
import colosseum.construction.data.MapDataImpl
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.bukkit.World
import java.io.File
import java.util.concurrent.*
import java.util.function.*

@ManagerDependency(WorldManager::class)
class MapDataManager: ConstructionSiteManager("MapData") {
    private val mapData: Cache<String, MapData> = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build()
    private val lock = Object()

    companion object {
        private fun getWorldManager(): WorldManager {
            return ConstructionSiteProvider.getSite().getManager(WorldManager::class.java)
        }

        private var getMapRootDir = Function<World, File> { world ->
            getWorldManager().getWorldFolder(world)
        }
    }

    override fun unregister() {
        synchronized(lock) {
            mapData.invalidateAll()
        }
    }

    fun get(world: World): MapData {
        val rootDirFile = getMapRootDir.apply(world)
        synchronized(lock) {
            var data: MapData?
            data = mapData.getIfPresent(rootDirFile.absolutePath)
            if (data == null) {
                val worldManager = getWorldManager()
                data = if (BaseUtils.isLevelNamePreserved(worldManager.getWorldRelativePath(world))) {
                    DummyMapData(world, rootDirFile)
                } else {
                    MapDataImpl(world, rootDirFile)
                }
                mapData.put(rootDirFile.absolutePath, data)
            }
            return data
        }
    }

    fun getFinalized(worldFolder: File): FinalizedMapData {
        val res = FinalizedMapData(MapDataImpl(null, worldFolder))
        return res
    }

    fun discard(world: World?) {
        if (world != null) {
            val rootDirFile = getMapRootDir.apply(world)
            synchronized(lock) {
                mapData.invalidate(rootDirFile.absolutePath)
            }
        }
    }
}
