package colosseum.construction.command

import colosseum.utility.UtilPlayerBase
import org.bukkit.entity.Player

class ItemClearLoreCommand : ItemCommand(
    listOf("clearlore"),
    "Clear lore of an item.",
    "/clearlore"
) {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        val item = caller.itemInHand
        val im = item.itemMeta

        im.lore = ArrayList()
        item.setItemMeta(im)
        caller.itemInHand = item

        UtilPlayerBase.sendMessage(caller, "Cleared lore on item!")
        return true
    }
}
