package colosseum.construction.manager

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.PluginUtils
import com.google.common.collect.Lists
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.logging.*

class SplashTextManager: ConstructionSiteManager("Splash text") {
    private var splashText: MutableList<String> = Lists.newCopyOnWriteArrayList()

    override fun register() {
        loadText()
    }

    override fun unregister() {
        saveText()
    }

    private fun getFile(): File {
        return PluginUtils.loadYml(ConstructionSiteProvider.getSite().pluginDataFolder, "join-text.yml")
    }

    private fun loadText() {
        val file: File = getFile()
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        val messages = config.getStringList("messages") ?: Lists.newCopyOnWriteArrayList()
        splashText = messages
    }

    private fun saveText() {
        val file: File = getFile()
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        config["messages"] = splashText
        try {
            config.save(file)
        } catch (e: IOException) {
            ConstructionSiteProvider.getSite().pluginLogger.log(Level.SEVERE, "Cannot save file", e)
        }
    }

    fun addText(s: String) {
        splashText.add(s)
    }

    fun clearText() {
        splashText.clear()
    }

    fun getText(): List<String> {
        return splashText
    }
}