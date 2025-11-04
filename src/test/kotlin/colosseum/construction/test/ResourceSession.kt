package colosseum.construction.test

import org.apache.commons.io.FileUtils
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ResourceSession : LauncherSessionListener {
    override fun launcherSessionOpened(session: LauncherSession) {
        try {
            val src = Paths.get("src", "test", "resources")
            path = Files.createTempDirectory("test-resources-")

            if (Files.exists(src)) {
                FileUtils.copyDirectory(src.toFile(), path.toFile())
            }
        } catch (e: Exception) {
            throw Error(e)
        }

        println("[JUnit] Copied test resources to $path")
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        FileUtils.deleteQuietly(path.toFile())
    }

    companion object {
        lateinit var path: Path
    }
}
