package colosseum.construction.test;

import colosseum.construction.PluginUtils;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite2;
import colosseum.utility.WorldMapConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

class TestPluginUtils {
    static DummySite plugin;
    @TempDir
    static File tempWorldContainer;

    @BeforeAll
    void setup() {
        tearDown();
        plugin = new DummySite2(tempWorldContainer);
        plugin.enable();
    }

    @AfterAll
    void tearDown() {
        Utils.tearDown(plugin);
    }

    @Test
    void testUnzip() {
        Assertions.assertDoesNotThrow(PluginUtils::unzip);
        File regionDir = tempWorldContainer.toPath().resolve(WorldMapConstants.WORLD).resolve(WorldMapConstants.REGION).toFile();
        File levelDat = tempWorldContainer.toPath().resolve(WorldMapConstants.WORLD).resolve(WorldMapConstants.LEVEL_DAT).toFile();
        Assertions.assertTrue(regionDir.exists());
        Assertions.assertTrue(regionDir.isDirectory());
        String[] mcas = new String[]{"r.0.0.mca", "r.0.-1.mca", "r.-1.0.mca", "r.-1.-1.mca"};
        for (String mca : mcas) {
            File f = new File(regionDir, mca);
            Assertions.assertTrue(f.exists());
            Assertions.assertTrue(f.isFile());
        }
        Assertions.assertTrue(levelDat.exists());
        Assertions.assertTrue(levelDat.isFile());
    }
}
