package colosseum.construction.manager

import colosseum.construction.Constants
import colosseum.construction.ConstructionSiteProvider

class ConfigManager: ConstructionSiteManager("Config") {
    override fun register() {
        create()

        val site = ConstructionSiteProvider.getSite()
        val config = site.config

        val keys = arrayOf(
            Constants.ConfigKeys.PARSE_MAXIMUM_RADIUS
        )

        validate()

        val logger = site.pluginLogger
        keys.forEach { k ->
            logger.info("Read $k: ${config.get(k)}")
        }
    }

    override fun unregister() {
        save()
    }

    private fun validate() {
        var r = false

        val config = ConstructionSiteProvider.getSite().config
        if (config.getInt(Constants.ConfigKeys.PARSE_MAXIMUM_RADIUS) < 10) {
            r = true
            config.set(Constants.ConfigKeys.PARSE_MAXIMUM_RADIUS, 10)
        }

        if (r) {
            save()
            reload()
        }
    }

    fun create() {
        ConstructionSiteProvider.getPlugin().saveDefaultConfig()
    }

    fun reload() {
        ConstructionSiteProvider.getPlugin().reloadConfig()
    }

    fun save() {
        ConstructionSiteProvider.getPlugin().saveConfig()
    }
}