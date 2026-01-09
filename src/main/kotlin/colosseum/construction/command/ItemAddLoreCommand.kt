package colosseum.construction.command

import colosseum.utility.UtilPlayerBase
import org.bukkit.entity.Player

class ItemAddLoreCommand : ItemCommand(
    listOf("addlore"),
    "Add a line of lore to item.",
    "/addlore <text>"
) {
    override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            return false
        }

        val item = caller.itemInHand
        val im = item.itemMeta

        val line = content(args)
        val lore: MutableList<String> = (if (im.hasLore()) ArrayList(im.lore) else ArrayList())
        lore.add(line.toString())
        im.lore = lore
        item.setItemMeta(im)
        caller.itemInHand = item

        UtilPlayerBase.sendMessage(caller, "Added lore: $line")
        return true
    }
}
