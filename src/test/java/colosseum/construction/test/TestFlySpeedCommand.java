package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import colosseum.construction.command.FlySpeedCommand;
import colosseum.construction.test.dummies.ConstructionPlayerMock;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite1;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

class TestFlySpeedCommand {
    private static DummySite plugin;
    private static ConstructionPlayerMock player;

    @TempDir
    static File tempPluginDataDir;

    @BeforeAll
    static void setup() {
        plugin = new DummySite1(tempPluginDataDir);
        player = new ConstructionPlayerMock("test");
        MockBukkit.getMock().addPlayer(player);
        plugin.setup();
    }

    @AfterAll
    static void tearDown() {
        plugin.teardown();
        MockBukkit.unload();
    }

    @Test
    void testPermission() {
        FlySpeedCommand command = new FlySpeedCommand();
        Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
        Assertions.assertFalse(command.canRun(player));
        player.setFlying(true);
        Assertions.assertTrue(command.canRun(player));
    }

    @Test
    void testInvalidInputs() {
        FlySpeedCommand command = new FlySpeedCommand();
        String label = command.getAliases().get(0);

        Assertions.assertFalse(command.runConstruction(player, label, new String[]{}));
        Assertions.assertFalse(command.runConstruction(player, label, new String[]{"a", "b"}));
        Assertions.assertFalse(command.runConstruction(player, label, new String[]{"a", "b", "c"}));
        Assertions.assertFalse(command.runConstruction(player, label, new String[]{"1.0", "b", "c"}));

        Assertions.assertFalse(command.runConstruction(player, label, new String[]{"a"}));
        Assertions.assertFalse(command.runConstruction(player, label, new String[]{"10.01"}));
        Assertions.assertFalse(command.runConstruction(player, label, new String[]{"-10.01"}));

        Assertions.assertFalse(command.runConstruction(player, label, new String[]{"0.99"}));
        Assertions.assertFalse(command.runConstruction(player, label, new String[]{"-10"}));
    }

    @Test
    void testValidInputs() {
        FlySpeedCommand command = new FlySpeedCommand();
        String label = command.getAliases().get(0);

        Assertions.assertTrue(command.runConstruction(player, label, new String[]{"1.0"}));
        Assertions.assertEquals(0.1f, player.getFlySpeed());
        Assertions.assertTrue(command.runConstruction(player, label, new String[]{"5.0"}));
        Assertions.assertEquals(0.5f, player.getFlySpeed());
        Assertions.assertTrue(command.runConstruction(player, label, new String[]{"2.34"}));
        Assertions.assertEquals(0.234f, player.getFlySpeed());
        Assertions.assertTrue(command.runConstruction(player, label, new String[]{"2.35"}));
        Assertions.assertEquals(0.235f, player.getFlySpeed());
        Assertions.assertTrue(command.runConstruction(player, label, new String[]{"9.99"}));
        Assertions.assertEquals(0.999f, player.getFlySpeed());
        Assertions.assertTrue(command.runConstruction(player, label, new String[]{"10"}));
        Assertions.assertEquals(1.0f, player.getFlySpeed());
    }
}
