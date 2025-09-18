package colosseum.construction.manager

import colosseum.construction.BaseUtils
import colosseum.construction.ConstructionSiteProvider
import colosseum.utility.WorldMapConstants
import org.apache.commons.lang.Validate
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import java.io.File
import java.util.*

class WorldManager: ConstructionSiteManager("Worlds") {
    override fun register() {
        Validate.isTrue(BaseUtils.initDir(getOnParseRootPath()), "Cannot initialize on parse root dir")
        Validate.isTrue(BaseUtils.initDir(getParsedZipOutputRootPath()), "Cannot initialize zip output root")
        getMapsRootPath().listFiles()?.forEach { f ->
            if (f.isDirectory) {
                val path = getWorldRelativePath(f)
                ConstructionSiteProvider.getSite().pluginLogger.info("Found $path, loading as a world.")
                loadWorld(path) ?: ConstructionSiteProvider.getSite().pluginLogger.warning("$path doesn't seem to be a valid world save.")
            }
        }
    }

    override fun unregister() {
        getMapsRootPath().listFiles()?.forEach { f ->
            if (f.isDirectory) {
                val path = getWorldRelativePath(f)
                loadWorld(path)?.let {
                    it.players.forEach { p -> p.kickPlayer("Construction site is closing!") }
                    unloadWorld(it, true)
                }
            }
        }
    }

    /**
     * @param worldName Relative path
     *
     * @return [WorldCreator] object
     *
     * @see [getWorldRelativePath]
     */
    fun getWorldCreator(worldName: String): WorldCreator {
        return WorldCreator(worldName)
    }

    /**
     * @param world [World] object.
     *
     * @return Directory that stores the world
     *
     * @see [World.getWorldFolder]
     */
    fun getWorldFolder(world: World): File {
        return world.worldFolder.absoluteFile
    }

    /**
     * Directory to all maps being parsed.
     */
    fun getOnParseRootPath(): File {
        return ConstructionSiteProvider.getSite().worldContainer.resolve(WorldMapConstants.PARSE).absoluteFile
    }

    fun getParsedZipOutputRootPath(): File {
        return ConstructionSiteProvider.getSite().pluginDataFolder.resolve(WorldMapConstants.PARSED).absoluteFile
    }

    fun getMapsRootPath(): File {
        return ConstructionSiteProvider.getSite().worldContainer.resolve(WorldMapConstants.MAP).absoluteFile
    }

    /**
     * @param filename single directory name
     *
     * @return Directory that stores the world
     */
    fun getSingleWorldRootPath(filename: String): File {
        return getMapsRootPath().resolve(filename).absoluteFile
    }

    /**
     * @param worldDir [File] object pointing to world directory
     *
     * @return Relative path in [String]
     *
     * @see [World.getWorldFolder]
     * @see [getWorldFolder]
     */
    fun getWorldRelativePath(worldDir: File): String {
        return ConstructionSiteProvider.getSite().worldContainer.absoluteFile.toPath().relativize(worldDir.absoluteFile.toPath()).toString()
    }

    fun getWorldRelativePath(world: World): String {
        return getWorldRelativePath(getWorldFolder(world))
    }

    /**
     * @param worldName Relative path
     *
     * @return [World] object or null
     *
     * @see [getWorldRelativePath]
     */
    fun loadWorld(worldName: String): World? {
        return createOrLoadWorld(getWorldCreator(worldName))
    }

    fun getWorldByUUID(uuid: UUID): World? {
        return Bukkit.getWorld(uuid)
    }

    /**
     * @param worldCreator [WorldCreator] with Relative path as name
     *
     * @return [World] object or null
     *
     * @see [getWorldRelativePath]
     */
    fun createOrLoadWorld(worldCreator: WorldCreator): World? {
        return Bukkit.createWorld(worldCreator)
    }

    fun unloadWorld(world: World?, save: Boolean) {
        if (world == null) {
            return
        }
        val path = getWorldRelativePath(world)
        if (!Bukkit.unloadWorld(world, save)) {
            throw RuntimeException("Cannot unload world \"$path\"!")
        }
        ConstructionSiteProvider.getSite().pluginLogger.info("Unloaded world \"$path\". World ${(if (save) "" else "not ")}saved.")
    }
}