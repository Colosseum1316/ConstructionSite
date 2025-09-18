package colosseum.construction

import colosseum.utility.WorldMapConstants
import colosseum.utility.arcade.GameType
import java.io.File

object BaseUtils {
    @JvmStatic
    fun initDir(dir: File): Boolean {
        if (!dir.exists()) {
            return dir.mkdirs()
        }
        return true
    }

    /**
     * @param name World <b>level</b> name
     * @return Is the name a preserved keyword
     */
    @JvmStatic
    fun isLevelNamePreserved(name: String): Boolean {
        return name.equals(WorldMapConstants.WORLD_LOBBY, ignoreCase = true) || name.equals(WorldMapConstants.WORLD, true)
    }

    @JvmStatic
    fun getGameTypes(): List<GameType> {
        return GameType.entries.stream().filter { it != GameType.None }.toList()
    }

    @JvmStatic
    fun determineGameType(raw: String?, noneOnError: Boolean): GameType {
        if (raw.isNullOrBlank()) {
            return GameType.None
        }
        if (noneOnError) {
            return try {
                GameType.valueOf(raw)
            } catch (t: Throwable) {
                GameType.None
            }
        }
        return GameType.valueOf(raw)
    }
}