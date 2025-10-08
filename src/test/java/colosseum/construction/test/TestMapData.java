package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite1;
import colosseum.construction.test.dummies.data.DummyMapDataRead;
import colosseum.construction.test.dummies.data.DummyMapDataWrite;
import colosseum.utility.MapData;
import colosseum.utility.WorldMapConstants;
import colosseum.utility.arcade.GameType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class TestMapData {
    private DummySite plugin;
    private WorldMock world;

    @TempDir
    static File tempWorldDir;
    @TempDir
    static File tempPluginDataDir;

    private static final String uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf";
    private PlayerMock player1;
    private static final String uuid2 = "07e79d0b-f86d-4bed-ae37-d87df8d94693";
    private PlayerMock player2;
    private static final String uuid3 = "3e65ea50-cd1a-45fb-81d7-7e27c14662d4";
    private PlayerMock player3;

    @BeforeAll
    void setup() {
        plugin = new DummySite1(tempPluginDataDir);
        world = MockBukkit.getMock().addSimpleWorld("world");
        player1 = new PlayerMock("test1", UUID.fromString(uuid1));
        player1.setOp(false);
        MockBukkit.getMock().addPlayer(player1);
        player2 = new PlayerMock("test2", UUID.fromString(uuid2));
        player2.setOp(false);
        MockBukkit.getMock().addPlayer(player2);
        player3 = new PlayerMock("test3", UUID.fromString(uuid3));
        player3.setOp(true);
        MockBukkit.getMock().addPlayer(player3);
        plugin.enable();
    }

    @AfterAll
    void tearDown() {
        plugin.disable();
        MockBukkit.unload();
    }

    @BeforeEach
    void setupEach() {
        File file = tempWorldDir.toPath().resolve(WorldMapConstants.MAP_DAT).toFile();
        ConstructionSiteProvider.getSite().getPluginLogger().info("Deleting " + file.getAbsolutePath());
        file.delete();
    }

    private DummyMapDataRead testRead0(final String testCase) {
        return Utils.readMapData(world, tempWorldDir, testCase);
    }

    @Order(1)
    @Test
    void testRead() {
        MapData data = testRead0(String.format("""
                
                currentlyLive:true
                warps:w1@(0,0,0);w2@(1,0,-1);w3@-1,-1,1;w4w4@0,0,0;
                MAP_NAME:Test Map_-Name123 456 789
                MAP_AUTHOR: Test Author_-Team123 456 789
                GAME_TYPE:
                ADMIN_LIST:%s,%s
                """.trim(), uuid1, uuid2));
        Assertions.assertTrue(data.isLive());
        Assertions.assertEquals(4, data.warps().size());
        Assertions.assertTrue(data.warps().containsKey("w1"));
        Assertions.assertEquals(data.warps().get("w1"), new Vector(0, 0, 0));
        Assertions.assertTrue(data.warps().containsKey("w2"));
        Assertions.assertEquals(data.warps().get("w2"), new Vector(1, 0, -1));
        Assertions.assertTrue(data.warps().containsKey("w3"));
        Assertions.assertEquals(data.warps().get("w3"), new Vector(-1, -1, 1));
        Assertions.assertTrue(data.warps().containsKey("w4w4"));
        Assertions.assertEquals(data.warps().get("w4w4"), new Vector(0, 0, 0));
        Assertions.assertEquals("Test Map_-Name123 456 789", data.getMapName());
        Assertions.assertEquals(" Test Author_-Team123 456 789", data.getMapCreator());
        Assertions.assertEquals(GameType.None, data.getMapGameType());
        Assertions.assertEquals(2, data.adminList().size());
        Assertions.assertTrue(data.adminList().stream().anyMatch(v -> v.toString().equals(uuid1)));
        Assertions.assertTrue(data.adminList().stream().anyMatch(v -> v.toString().equals(uuid2)));
        Assertions.assertTrue(data.allows(player1));
        Assertions.assertTrue(data.allows(player2));
        Assertions.assertTrue(data.allows(player3));

        data = testRead0(String.format("""
                :invalid
                currentlyLive:false
                warps:;
                MAP_NAME:TEST MAP
                MAP_AUTHOR:TEST AUTHOR
                GAME_TYPE:DragonEscape
                ADMIN_LIST:%s
                """.trim(), uuid1));
        Assertions.assertFalse(data.isLive());
        Assertions.assertTrue(data.warps().isEmpty());
        Assertions.assertEquals("TEST MAP", data.getMapName());
        Assertions.assertEquals("TEST AUTHOR", data.getMapCreator());
        Assertions.assertEquals(GameType.DragonEscape, data.getMapGameType());
        Assertions.assertEquals(1, data.adminList().size());
        Assertions.assertTrue(data.adminList().stream().anyMatch(v -> v.toString().equals(uuid1)));
        Assertions.assertFalse(data.adminList().stream().anyMatch(v -> v.toString().equals(uuid2)));
        Assertions.assertTrue(data.allows(player1));
        Assertions.assertFalse(data.allows(player2));
        Assertions.assertTrue(data.allows(player3));
    }

    @FunctionalInterface
    private interface DummyMapWriteAssertionCallback {
        void assertion(MapData data, String mapName, String mapCreator, GameType mapGameType, Map<String, Vector> warps, Set<UUID> adminList, boolean currentlyLive);
    }

    private void testWrite0(
            String mapName,
            String mapCreator,
            GameType mapGameType,
            Map<String, Vector> warps,
            Set<UUID> adminList,
            boolean currentlyLive,
            DummyMapWriteAssertionCallback assertion
    ) {
        new DummyMapDataWrite(world, tempWorldDir, mapName, mapCreator, mapGameType, warps, adminList, currentlyLive).write();
        MapData data = Utils.readMapData(world, tempWorldDir);
        assertion.assertion(data, mapName, mapCreator, mapGameType, warps, adminList, currentlyLive);
    }

    @Order(2)
    @Test
    void testWrite() {
        testWrite0("TEST MAP NONETYPE", "TEST MAP NONETYPE AUTHOR", GameType.None, Collections.emptyMap(), Collections.emptySet(), true, (data, mapName, mapCreator, mapGameType, warps, adminList, currentlyLive) -> {
            Assertions.assertTrue(data.isLive());
            Assertions.assertTrue(data.warps().isEmpty());
            Assertions.assertTrue(data.adminList().isEmpty());
            Assertions.assertEquals(GameType.None, data.getMapGameType());
            Assertions.assertEquals("TEST MAP NONETYPE", data.getMapName());
            Assertions.assertEquals("TEST MAP NONETYPE AUTHOR", data.getMapCreator());
        });
        testWrite0("TEST MAP 1", "TEST MAP 1 AUTHOR", GameType.DragonEscape, Map.of(
                "a1", new Vector(0, 0, 0),
                "a2", new Vector( 0, 1, 0),
                "a3", new Vector( -1, 0, 1)
        ), Set.of(
                UUID.fromString(uuid1),
                UUID.fromString(uuid2)
        ), false, (data, mapName, mapCreator, mapGameType, warps, adminList, currentlyLive) -> {
            Assertions.assertFalse(data.isLive());
            Assertions.assertEquals(3, data.warps().size());
            Assertions.assertTrue(data.warps().containsKey("a1"));
            Assertions.assertEquals(data.warps().get("a1"), new Vector(0, 0, 0));
            Assertions.assertTrue(data.warps().containsKey("a2"));
            Assertions.assertEquals(data.warps().get("a2"), new Vector(0, 1, 0));
            Assertions.assertTrue(data.warps().containsKey("a3"));
            Assertions.assertEquals(data.warps().get("a3"), new Vector(-1, 0, 1));
            Assertions.assertEquals(2, data.adminList().size());
            Assertions.assertTrue(data.adminList().stream().anyMatch(v -> v.toString().equals(uuid1)));
            Assertions.assertTrue(data.adminList().stream().anyMatch(v -> v.toString().equals(uuid2)));
            Assertions.assertEquals(GameType.DragonEscape, data.getMapGameType());
            Assertions.assertEquals("TEST MAP 1", data.getMapName());
            Assertions.assertEquals("TEST MAP 1 AUTHOR", data.getMapCreator());
            Assertions.assertTrue(data.allows(player1));
            Assertions.assertTrue(data.allows(player2));
            Assertions.assertTrue(data.allows(player3));
        });
    }
}
