package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import colosseum.construction.GameTypeUtils;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite2;
import colosseum.utility.arcade.GameType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

class TestGameTypeUtils {
    private DummySite plugin;
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
    void testUtilities() {
        Assertions.assertFalse(GameTypeUtils.getGameTypes().contains(GameType.None));
        Assertions.assertEquals(GameType.None, GameTypeUtils.determineGameType(null, false));
        Assertions.assertEquals(GameType.None, GameTypeUtils.determineGameType("", false));
        Assertions.assertEquals(GameType.None, GameTypeUtils.determineGameType("  ", false));
        Assertions.assertThrows(Exception.class, () -> {
            GameTypeUtils.determineGameType("no", false);
            GameTypeUtils.determineGameType("123456", false);
        });
        Assertions.assertDoesNotThrow(() -> {
            Assertions.assertEquals(GameType.None, GameTypeUtils.determineGameType("no", true));
            Assertions.assertEquals(GameType.None, GameTypeUtils.determineGameType("123456", true));
        });
    }

    @Test
    void testMessage() {
        PlayerMock player = MockBukkit.getMock().addPlayer();
        GameTypeUtils.printValidGameTypes(player);
        String message = player.nextMessage();
        player.assertNoMoreSaid();
        Assertions.assertNotNull(message);
        Assertions.assertTrue(message.startsWith("§cValid game types:"));
        Assertions.assertFalse(message.contains("None"));
    }
}
