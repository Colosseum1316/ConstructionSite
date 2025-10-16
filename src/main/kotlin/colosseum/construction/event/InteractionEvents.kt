package colosseum.construction.event

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.manager.MapDataManager
import colosseum.construction.manager.SplashTextManager
import colosseum.construction.manager.TeleportManager
import colosseum.utility.UtilPlayerBase.sendMessage
import org.bukkit.GameMode
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockCanBuildEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.block.EntityBlockFormEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityBreakDoorEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.weather.LightningStrikeEvent
import org.bukkit.event.weather.ThunderChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent
import java.util.function.*

class InteractionEvents: ConstructionSiteEventListener() {

    private fun disableInteraction(player: Player, event: Cancellable) {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(player.world))) {
            event.isCancelled = true
        }
        if (!ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java).get(player.world).allows(player)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerInteraction(event: PlayerBedEnterEvent) {
        disableInteraction(event.player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerInteraction(event: PlayerEditBookEvent) {
        disableInteraction(event.player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerInteraction(event: PlayerFishEvent) {
        disableInteraction(event.player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerInteraction(event: PlayerPortalEvent) {
        disableInteraction(event.player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerInteraction(event: PlayerShearEntityEvent) {
        disableInteraction(event.player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerInteraction(event: PlayerInteractEvent) {
        disableInteraction(event.player, event)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoinTeleportToSpawn(event: PlayerJoinEvent) {
        ConstructionSiteProvider.getSite().getManager(TeleportManager::class.java).teleportToServerSpawn(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoinPrintSplashText(event: PlayerJoinEvent) {
        ConstructionSiteProvider.getSite().getManager(SplashTextManager::class.java).getText().forEach(Consumer { v: String -> sendMessage(event.player, v) })
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(event.player.world))) {
            event.isCancelled = event.newGameMode != GameMode.ADVENTURE && event.newGameMode != GameMode.SPECTATOR
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.to.y <= 0) {
            event.to = event.player.world.spawnLocation
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onTeleport(event: PlayerTeleportEvent) {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(event.to.world))) {
            event.player.gameMode = GameMode.ADVENTURE
            event.player.isFlying = false
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockDamage(event: BlockDamageEvent) {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(event.player.world))) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(event.player.world))) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(event.player.world))) {
            event.setBuild(false)
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockCanBuild(event: BlockCanBuildEvent) {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(event.block.world))) {
            event.isBuildable = false
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockDispense(event: BlockDispenseEvent) {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(event.block.world))) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(event.block.world))) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onRedstoneEvent(event: BlockRedstoneEvent) {
        if (WorldUtils.isLevelNamePreserved(WorldUtils.getWorldRelativePath(event.block.world))) {
            event.newCurrent = event.oldCurrent
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        event.isCancelled = event.entityType == EntityType.PLAYER && (event.cause != EntityDamageEvent.DamageCause.VOID && event.cause != EntityDamageEvent.DamageCause.CUSTOM && event.cause != EntityDamageEvent.DamageCause.SUICIDE)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntitySpawn(event: EntitySpawnEvent) {
        event.isCancelled = event.entityType != EntityType.ARMOR_STAND
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityBreakDoor(event: EntityBreakDoorEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disableBurn(event: BlockBurnEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disableIgnite(event: BlockIgniteEvent) {
        event.isCancelled = event.cause == BlockIgniteEvent.IgniteCause.LAVA || event.cause == BlockIgniteEvent.IgniteCause.SPREAD
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disableSpread(event: BlockSpreadEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disableFade(event: BlockFadeEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disableDecay(event: LeavesDecayEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disableForm(event: BlockFormEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disableForm(event: EntityBlockFormEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disableWeather(event: WeatherChangeEvent) {
        event.isCancelled = event.toWeatherState()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disableThunder(event: ThunderChangeEvent) {
        event.isCancelled = event.toThunderState()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disableThunder(event: LightningStrikeEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun disablePortal(event: EntityPortalEvent) {
        event.isCancelled = true
    }
}