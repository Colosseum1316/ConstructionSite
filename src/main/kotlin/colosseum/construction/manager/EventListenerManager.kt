package colosseum.construction.manager

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.event.InteractionEvents
import colosseum.construction.event.TreeEvents
import org.bukkit.event.HandlerList

@ManagerDependency(MapDataManager::class, WorldManager::class, TeleportManager::class)
class EventListenerManager: ConstructionSiteManager("Events") {
    private lateinit var interactionEvents: InteractionEvents
    private lateinit var treeEvents: TreeEvents

    override fun register() {
        interactionEvents = InteractionEvents()
        treeEvents = TreeEvents()
        registerEvents()
    }

    override fun unregister() {
        unregisterEvents()
    }

    private fun registerEvents() {
        val plugin = ConstructionSiteProvider.getPlugin()
        ConstructionSiteProvider.getSite().getServer().pluginManager.registerEvents(interactionEvents, plugin)
        ConstructionSiteProvider.getSite().getServer().pluginManager.registerEvents(treeEvents, plugin)
    }

    private fun unregisterEvents() {
        treeEvents?.let { HandlerList.unregisterAll(it) }
        interactionEvents?.let { HandlerList.unregisterAll(it) }
    }
}