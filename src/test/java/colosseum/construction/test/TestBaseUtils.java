package colosseum.construction.test;

import colosseum.construction.BaseUtils;
import colosseum.utility.WorldMapConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestBaseUtils {
    @Test
    void testPreservedLevelName() {
        Assertions.assertTrue(BaseUtils.isLevelNamePreserved(WorldMapConstants.WORLD));
        Assertions.assertTrue(BaseUtils.isLevelNamePreserved(WorldMapConstants.WORLD_LOBBY));
        Assertions.assertTrue(BaseUtils.isLevelNamePreserved("World_Lobby"));
        Assertions.assertTrue(BaseUtils.isLevelNamePreserved("world_Lobby"));
        Assertions.assertFalse(BaseUtils.isLevelNamePreserved("world-lobby"));
        Assertions.assertTrue(BaseUtils.isLevelNamePreserved("worlD"));
        Assertions.assertTrue(BaseUtils.isLevelNamePreserved("World"));
        Assertions.assertFalse(BaseUtils.isLevelNamePreserved("map/World"));
    }
}
