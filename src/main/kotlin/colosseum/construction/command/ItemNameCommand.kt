package colosseum.construction.command

import colosseum.utility.UtilPlayerBase
import org.bukkit.entity.Player

class ItemNameCommand : ItemCommand(
    listOf("itemname"),
    "Set display name of an item.",
    "/itemname <Display name>"
) {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            return false
        }

        val item = caller.itemInHand
        val im = item.itemMeta

        val name = determineContent(args)
        im.displayName = name
        item.setItemMeta(im)
        caller.itemInHand = item

        UtilPlayerBase.sendMessage(caller, "Set item name: $name")
        return true
    }
}
