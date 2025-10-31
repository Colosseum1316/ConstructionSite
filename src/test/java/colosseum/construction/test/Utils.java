package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.ConstructionSite;
import colosseum.construction.WorldUtils;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.data.DummyMapDataRead;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.World;
import org.junit.jupiter.api.Assertions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {
    public static void writeMapData(final File worldDir, final String testCase) {
        File mapDat = WorldUtils.mapDatFile(worldDir);
        Assertions.assertDoesNotThrow(() -> {
            try (FileWriter writer = new FileWriter(mapDat); BufferedWriter buffer = new BufferedWriter(writer)) {
                buffer.write(testCase);
            }
        });
    }

    public static DummyMapDataRead readMapData(final World world, final File worldDir, final String testCase) {
        writeMapData(worldDir, testCase);
        return readMapData(world, worldDir);
    }

    public static DummyMapDataRead readMapData(final World world, final File worldDir) {
        return new DummyMapDataRead(world, worldDir);
    }

    public static Logger getSiteLogger(ConstructionSite site) {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        Logger logger;
        logger = Logger.getLogger(site.getClass().getSimpleName());
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        return logger;
    }

    // @BeforeAll and @AfterAll are terribly terrible
    public static void tearDown(DummySite site) {
        try {
            site.disable();
        } catch (Exception e) {
            // no op
        }
        if (MockBukkit.getMock() != null) {
            MockBukkit.unload();
        }
    }
}
