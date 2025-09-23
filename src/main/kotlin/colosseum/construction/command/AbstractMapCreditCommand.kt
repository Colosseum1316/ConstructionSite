package colosseum.construction.command

import colosseum.utility.MutableMapData
import org.bukkit.entity.Player
import java.util.function.*

abstract class AbstractMapCreditCommand protected constructor(
    aliases: List<String>,
    description: String,
    usage: String
): AbstractMapAdminCommand(
    aliases, description, usage
) {

    protected fun content(args: Array<String>): StringBuilder {
        val content = args.joinToString(" ").trim { it <= ' ' }
        return StringBuilder(content)
    }

    protected abstract fun determineContent(caller: Player, alias: String, args: Array<String>): String
    protected abstract fun setField(caller: Player, alias: String, args: Array<String>, contentSupplier: Supplier<String>, mapDataSupplier: Supplier<MutableMapData>)
    protected abstract fun postAction(caller: Player, alias: String, args: Array<String>, contentSupplier: Supplier<String>, mapDataSupplier: Supplier<MutableMapData>): Boolean

    final override fun runConstruction(caller: Player, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            return false
        }
        val content = determineContent(caller, label, args)
        val world = caller.world
        val data = getMapDataManager().get(world)
        setField(caller, label, args, { content }, { data as MutableMapData })
        return postAction(caller, label, args, { content }, { data as MutableMapData })
    }
}