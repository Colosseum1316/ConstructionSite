package colosseum.construction.manager

import colosseum.construction.Constants.ConfigKeys.PARSE__MAXIMUM_RADIUS
import colosseum.construction.ConstructionSiteProvider

class ConfigManager: ConstructionSiteManager("Config") {
    override fun register() {
        create()

        val site = ConstructionSiteProvider.getSite()
        val config = site.config

        val defaults = mapOf<String, Any>(
            PARSE__MAXIMUM_RADIUS to 1000
        )
        defaults.forEach { (k, v) ->
            config.addDefault(k, v)
        }
        config.options().copyDefaults(true)
        save()

        validate()

        defaults.keys.forEach { k ->
            site.pluginLogger.info("Read $k: ${config.get(k)}")
        }
    }

    override fun unregister() {
        save()
    }

    private fun validate() {
        var r = false

        val config = ConstructionSiteProvider.getSite().config
        if (config.getInt(PARSE__MAXIMUM_RADIUS) < 10) {
            r = true
            config.set(PARSE__MAXIMUM_RADIUS, 10)
        }

        if (r) {
            save()
            reload()
        }
    }

    private fun create() {
        ConstructionSiteProvider.getPlugin().saveDefaultConfig()
    }

    private fun reload() {
        ConstructionSiteProvider.getPlugin().reloadConfig()
    }

    private fun save() {
        ConstructionSiteProvider.getPlugin().saveConfig()
    }
}