package colosseum.construction.manager

import colosseum.construction.BaseUtils
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import org.apache.commons.lang.Validate

class WorldManager : ConstructionSiteManager("Worlds") {
    override fun register() {
        Validate.isTrue(BaseUtils.initDir(WorldUtils.getOnParseRootPath()), "Cannot initialize on parse root dir")
        Validate.isTrue(BaseUtils.initDir(WorldUtils.getParsedZipOutputRootPath()), "Cannot initialize zip output root")
        WorldUtils.getMapsRootPath().listFiles()?.forEach { f ->
            if (f.isDirectory) {
                val path = WorldUtils.getWorldRelativePath(f)
                WorldUtils.loadWorld(path)
                    ?: ConstructionSiteProvider.getSite().pluginLogger.warning("$path doesn't seem to be a valid world save.")
            }
        }
    }

    override fun unregister() {
        WorldUtils.getMapsRootPath().listFiles()?.forEach { f ->
            if (f.isDirectory) {
                val path = WorldUtils.getWorldRelativePath(f)
                WorldUtils.loadWorld(path)?.let {
                    it.players.forEach { p -> p.kickPlayer("Construction site is closing!") }
                    WorldUtils.unloadWorld(it, true)
                }
            }
        }
    }
}