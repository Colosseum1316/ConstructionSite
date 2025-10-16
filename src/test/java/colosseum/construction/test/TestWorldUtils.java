package colosseum.construction.test;

import colosseum.construction.WorldUtils;
import colosseum.utility.WorldMapConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestWorldUtils {
    @Test
    void testPreservedLevelName() {
        Assertions.assertDoesNotThrow(() -> WorldUtils.unloadWorld(null, true));
        Assertions.assertTrue(WorldUtils.isLevelNamePreserved(WorldMapConstants.WORLD));
        Assertions.assertTrue(WorldUtils.isLevelNamePreserved(WorldMapConstants.WORLD_LOBBY));
        Assertions.assertTrue(WorldUtils.isLevelNamePreserved("World_Lobby"));
        Assertions.assertTrue(WorldUtils.isLevelNamePreserved("world_Lobby"));
        Assertions.assertFalse(WorldUtils.isLevelNamePreserved("world-lobby"));
        Assertions.assertTrue(WorldUtils.isLevelNamePreserved("worlD"));
        Assertions.assertTrue(WorldUtils.isLevelNamePreserved("World"));
        Assertions.assertFalse(WorldUtils.isLevelNamePreserved("map/World"));
    }
}
