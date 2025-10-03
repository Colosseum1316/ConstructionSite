package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.command.AbstractTeleportCommand;
import colosseum.construction.command.TeleportHubCommand;
import colosseum.construction.command.TeleportMapCommand;
import colosseum.construction.command.TeleportSpawnCommand;
import colosseum.construction.command.TeleportWarpCommand;
import colosseum.construction.command.vanilla.TeleportCommand;
import colosseum.construction.manager.TeleportManager;
import colosseum.construction.test.dummies.ConstructionSitePlayerMock;
import colosseum.construction.test.dummies.ConstructionSiteServerMock;
import colosseum.construction.test.dummies.ConstructionSiteWorldMock;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite3;
import colosseum.construction.test.dummies.data.DummyMapDataRead;
import colosseum.utility.WorldMapConstants;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.UUID;

public class TestTeleportCommands {
    private static DummySite plugin;
    private static final String uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf";
    private static ConstructionSitePlayerMock player1;
    private static final String uuid2 = "07e79d0b-f86d-4bed-ae37-d87df8d94693";
    private static ConstructionSitePlayerMock player2;
    private static ConstructionSiteWorldMock world;
    private static ConstructionSiteWorldMock worldLobby;
    private static ConstructionSiteWorldMock worldMap;
    private static DummyMapDataRead mapData;

    @TempDir
    static File tempWorldContainer;
    @TempDir
    static File tempPluginDataDir;

    @BeforeAll
    static void setup() {
        plugin = new DummySite3(tempWorldContainer, tempPluginDataDir);
        world = new ConstructionSiteWorldMock(WorldMapConstants.WORLD);
        worldLobby = new ConstructionSiteWorldMock(WorldMapConstants.WORLD_LOBBY);
        worldMap = new ConstructionSiteWorldMock("test_map");
        player1 = new ConstructionSitePlayerMock("test1", UUID.fromString(uuid1));
        player1.setOp(false);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addPlayer(player1);
        player2 = new ConstructionSitePlayerMock("test2", UUID.fromString(uuid2));
        player2.setOp(true);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addPlayer(player2);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(world);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(worldLobby);
        ((ConstructionSiteServerMock) MockBukkit.getMock()).addWorld(worldMap);
        plugin.setup();
        Assertions.assertTrue(worldMap.getWorldFolder().mkdirs());
        mapData = Utils.readMapData(worldMap, worldMap.getWorldFolder(), String.format("""
                currentlyLive:true
                warps:k1@0,0,0;k2@0,1,0;
                MAP_NAME:Test map
                MAP_AUTHOR:Test author
                GAME_TYPE:None
                ADMIN_LIST:%s
                """.trim(), uuid1));
    }

    @AfterAll
    static void tearDown() {
        plugin.teardown();
        MockBukkit.unload();
    }

    @Test
    void testPermission() {
        AbstractTeleportCommand[] commands = new AbstractTeleportCommand[] {
                new TeleportCommand(),
                new TeleportSpawnCommand(),
                new TeleportHubCommand(),
                new TeleportMapCommand()
        };
        for (AbstractTeleportCommand command : commands) {
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
            Assertions.assertTrue(command.canRun(player1));
            Assertions.assertTrue(command.canRun(player2));
        }
        AbstractTeleportCommand warpCommand = new TeleportWarpCommand();
        TeleportManager manager = ConstructionSiteProvider.getSite().getManager(TeleportManager.class);
        manager.teleportToServerSpawn(player1);
        manager.teleportToServerSpawn(player2);
        Assertions.assertFalse(warpCommand.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(warpCommand.canRun(player1));
        Assertions.assertFalse(warpCommand.canRun(player2));
        manager.teleportPlayer(player1, new Location(worldLobby, 0, 0, 0));
        manager.teleportPlayer(player2, new Location(worldLobby, 0, 0, 0));
        Assertions.assertFalse(warpCommand.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(warpCommand.canRun(player1));
        Assertions.assertFalse(warpCommand.canRun(player2));
        manager.teleportPlayer(player1, new Location(worldMap, 0, 0, 0));
        manager.teleportPlayer(player2, new Location(worldMap, 0, 0, 0));
        Assertions.assertFalse(warpCommand.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertTrue(warpCommand.canRun(player1));
        Assertions.assertTrue(warpCommand.canRun(player2));
    }
}
