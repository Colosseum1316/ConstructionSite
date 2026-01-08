package colosseum.construction.event

import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.manager.MapDataManager
import colosseum.utility.BlockData
import colosseum.utility.UtilBlockBase.getSurrounding
import colosseum.utility.UtilEvent.ActionType
import colosseum.utility.UtilEvent.isAction
import colosseum.utility.UtilGear.isMaterial
import colosseum.utility.UtilPlayerBase.sendMessage
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.collect.Sets
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.*
import java.util.function.*

@Suppress("UnstableApiUsage")
class TreeEvents : ConstructionSiteEventListener() {
    private val treeHistory: Cache<UUID, MutableList<Set<BlockData>>> =
        CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        treeHistory.invalidate(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun treeRemover(event: PlayerInteractEvent) {
        val player = event.player
        // Permission
        if (!ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java).get(player.world)
                .allows(player)
        ) {
            return
        }
        if (!isMaterial(player.itemInHand, Material.NETHER_STAR)) {
            return
        }

        event.isCancelled = true
        // Remove
        if (isAction(event, ActionType.L_BLOCK)) {
            val block = event.clickedBlock
            if (block.type != Material.LOG) {
                sendMessage(player, "&cLeft-click on a log!")
                return
            }

            val toRemove = searchLogBlock(Sets.newHashSet(), block)
            if (toRemove.isEmpty()) {
                sendMessage(player, "&cLeft-click on a log!")
                return
            }

            val history: MutableSet<BlockData> = Sets.newHashSet()
            for (i in toRemove) {
                history.add(BlockData(i))
                i.type = Material.AIR
            }

            var h = treeHistory.getIfPresent(player.uniqueId)
            if (h == null) {
                h = ArrayList()
                treeHistory.put(player.uniqueId, h)
            }
            h.add(0, history)
            h.let {
                while (it.size > 10) {
                    it.removeAt(10)
                }
            }
            sendMessage(player, "&cTree removed.")
        } else if (isAction(event, ActionType.R)) {
            val h = treeHistory.getIfPresent(player.uniqueId).takeIf { it.isNullOrEmpty().not() } ?: run {
                sendMessage(player, "&cNo tree history!")
                return
            }

            val data: Set<BlockData> = h.removeAt(0)
            data.forEach(Consumer { obj: BlockData -> obj.restore(true) })
            sendMessage(player, "&aTree restored.")
        }
    }

    companion object {
        @JvmStatic
        fun searchLogBlock(blocks: MutableSet<Block>, current: Block): Set<Block> {
            // Not Tree
            if (current.type != Material.LOG && current.type != Material.LEAVES) {
                return blocks
            }

            if (!blocks.add(current)) {
                return blocks
            }

            for (other in getSurrounding(current, true)) {
                if (current.type != Material.LOG && current.type != Material.LEAVES) {
                    continue
                }
                if (blocks.contains(other)) {
                    continue
                }
                // Don't spread from leaves to log
                if (current.type == Material.LEAVES && other.type == Material.LOG) {
                    continue
                }
                searchLogBlock(blocks, other)
            }
            return blocks
        }
    }
}