package colosseum.construction

import colosseum.construction.WorldUtils.getWorldFolder
import colosseum.construction.WorldUtils.getWorldRelativePath
import colosseum.utility.WorldMapConstants
import org.bukkit.World
import org.bukkit.WorldCreator
import java.io.File
import java.util.*

object WorldUtils {
    /**
     * @param name World <b>level</b> name
     * @return Is the name a preserved keyword
     */
    @JvmStatic
    fun isLevelNamePreserved(name: String): Boolean {
        return name.equals(WorldMapConstants.WORLD_LOBBY, ignoreCase = true) || name.equals(WorldMapConstants.WORLD, ignoreCase = true)
    }

    /**
     * @param worldName Relative path
     *
     * @return [WorldCreator] object
     *
     * @see [getWorldRelativePath]
     */
    @JvmStatic
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
    @JvmStatic
    fun getWorldFolder(world: World): File {
        return world.worldFolder.absoluteFile
    }

    /**
     * Directory to all maps being parsed.
     */
    @JvmStatic
    fun getOnParseRootPath(): File {
        return ConstructionSiteProvider.getSite().worldContainer.resolve(WorldMapConstants.PARSE).absoluteFile
    }

    @JvmStatic
    fun getParsedZipOutputRootPath(): File {
        return ConstructionSiteProvider.getSite().pluginDataFolder.resolve(WorldMapConstants.PARSED).absoluteFile
    }

    @JvmStatic
    fun getMapsRootPath(): File {
        return ConstructionSiteProvider.getSite().worldContainer.resolve(WorldMapConstants.MAP).absoluteFile
    }

    /**
     * @param filename single directory name
     *
     * @return Directory that stores the world
     */
    @JvmStatic
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
    @JvmStatic
    fun getWorldRelativePath(worldDir: File): String {
        return ConstructionSiteProvider.getSite().worldContainer.absoluteFile.toPath().relativize(worldDir.absoluteFile.toPath()).toString()
    }

    @JvmStatic
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
    @JvmStatic
    fun loadWorld(worldName: String): World? {
        return createOrLoadWorld(getWorldCreator(worldName))
    }

    @JvmStatic
    fun getWorldByUUID(uuid: UUID): World? {
        return ConstructionSiteProvider.getSite().getServer().getWorld(uuid)
    }

    /**
     * @param worldCreator [WorldCreator] with Relative path as name
     *
     * @return [World] object or null
     *
     * @see [getWorldRelativePath]
     */
    @JvmStatic
    fun createOrLoadWorld(worldCreator: WorldCreator): World? {
        return ConstructionSiteProvider.getSite().getServer().createWorld(worldCreator)
    }

    @JvmStatic
    @Throws(Exception::class)
    fun unloadWorld(world: World?, save: Boolean) {
        if (world == null) {
            return
        }
        val path = getWorldRelativePath(world)
        if (!ConstructionSiteProvider.getSite().getServer().unloadWorld(world, save)) {
            throw RuntimeException("Cannot unload world \"$path\"!")
        }
        ConstructionSiteProvider.getSite().pluginLogger.info("Unloaded world \"$path\". World ${(if (save) "" else "not ")}saved.")
    }
}