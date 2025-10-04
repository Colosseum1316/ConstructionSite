package colosseum.construction

import java.io.File

object BaseUtils {
    @JvmStatic
    fun initDir(dir: File): Boolean {
        if (!dir.exists()) {
            return dir.mkdirs()
        }
        return true
    }
}