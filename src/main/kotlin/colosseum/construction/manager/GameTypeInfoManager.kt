package colosseum.construction.manager

import colosseum.construction.BaseUtils
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.PluginUtils
import colosseum.utility.GameTypeInfo
import colosseum.utility.arcade.GameType
import com.google.common.collect.Maps
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.logging.*

class GameTypeInfoManager: ConstructionSiteManager("GameType Info") {
    private val gameTypeInfoMap: MutableMap<GameType, GameTypeInfo> = Maps.newConcurrentMap()

    override fun register() {
        loadInfo()
    }

    override fun unregister() {
        gameTypeInfoMap.values.forEach { info: GameTypeInfo -> saveInfo(info) }
    }

    private fun loadInfo() {
        val file: File = PluginUtils.loadYml(ConstructionSiteProvider.getSite().pluginDataFolder, "info.yml")
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)

        for (s in config.getKeys(false)) {
            val gameType: GameType = BaseUtils.determineGameType(s, false)
            val info = config.getStringList(s)
            gameTypeInfoMap[gameType] = GameTypeInfo(gameType, info)
        }
    }

    private fun saveInfo(obj: GameTypeInfo) {
        val site = ConstructionSiteProvider.getSite()
        val file: File = PluginUtils.loadYml(site.pluginDataFolder, "info.yml")
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        config[obj.gameType.name] = obj.info
        try {
            site.pluginLogger.info("Saving ${file.absolutePath}")
            config.save(file)
        } catch (e: IOException) {
            site.pluginLogger.log(Level.SEVERE, "Cannot save file", e)
        }
    }

    fun getGameTypeInfo(type: GameType): GameTypeInfo? {
        return gameTypeInfoMap[type]
    }

    fun setGameTypeInfo(gameType: GameType, info: GameTypeInfo) {
        gameTypeInfoMap[gameType] = info
    }
}