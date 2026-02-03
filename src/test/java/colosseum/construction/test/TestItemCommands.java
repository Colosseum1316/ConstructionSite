package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import colosseum.construction.command.ItemAddLoreCommand;
import colosseum.construction.command.ItemClearLoreCommand;
import colosseum.construction.command.ItemCommand;
import colosseum.construction.command.ItemNameCommand;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite1;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Arrays;

class TestItemCommands {
    private DummySite plugin;
    private PlayerMock player;

    @TempDir
    static File tempPluginDataDir;

    @BeforeAll
    void setup() {
        tearDown();
        plugin = new DummySite1(tempPluginDataDir);
        player = MockBukkit.getMock().addPlayer();
        plugin.enable();
    }

    @AfterAll
    void tearDown() {
        Utils.tearDown(plugin);
    }

    @Test
    void test() {
        ItemCommand[] commands = new ItemCommand[]{
                new ItemAddLoreCommand(),
                new ItemClearLoreCommand(),
                new ItemNameCommand()
        };
        for (ItemCommand command : commands) {
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
            Assertions.assertFalse(command.canRun(player));
            player.assertSaid("§cHold an item in your hand!");
            player.assertNoMoreSaid();

            ItemStack item = new ItemStack(Material.GLASS);
            player.setItemInHand(item);
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
            Assertions.assertTrue(command.canRun(player));
            player.assertNoMoreSaid();

            item = new ItemStack(Material.AIR);
            player.setItemInHand(item);
            Assertions.assertFalse(command.canRun(MockBukkit.getMock().getConsoleSender()));
            Assertions.assertFalse(command.canRun(player));
            player.assertSaid("§cHold an item in your hand!");
            player.assertNoMoreSaid();
        }

        // Test adding and clearing item lore.

        player.assertNoMoreSaid();

        ItemCommand commandAddLore = new ItemAddLoreCommand();
        String label = commandAddLore.getAliases().get(0);
        ItemStack item = new ItemStack(Material.GLASS);
        player.setItemInHand(new ItemStack(Material.AIR));
        player.setItemInHand(item);

        Assertions.assertFalse(commandAddLore.runConstruction(player, label, new String[]{}));
        Assertions.assertTrue(commandAddLore.runConstruction(player, label, new String[]{"A", "line", "of", "lore."}));
        Assertions.assertTrue(commandAddLore.runConstruction(player, label, new String[]{"Another", "line", "&cof", "lore."}));
        player.assertSaid("Added lore: A line of lore.");
        player.assertSaid("Added lore: Another line §cof lore.");
        player.assertNoMoreSaid();

        item = player.getItemInHand();
        Assertions.assertEquals(2, item.getItemMeta().getLore().size());
        Assertions.assertLinesMatch(Arrays.asList(
                "A line of lore.",
                "Another line §cof lore."
        ), item.getItemMeta().getLore());

        ItemCommand commandClearLore = new ItemClearLoreCommand();
        label = commandClearLore.getAliases().get(0);
        Assertions.assertTrue(commandClearLore.runConstruction(player, label, new String[]{}));
        player.assertSaid("Cleared lore on item!");
        player.assertNoMoreSaid();

        item = player.getItemInHand();
        Assertions.assertEquals(0, item.getItemMeta().getLore().size());

        // Testing against item name

        player.assertNoMoreSaid();

        ItemCommand command = new ItemNameCommand();
        label = command.getAliases().get(0);
        item = new ItemStack(Material.GLASS);
        player.setItemInHand(new ItemStack(Material.AIR));
        player.setItemInHand(item);

        Assertions.assertFalse(command.runConstruction(player, label, new String[]{}));
        Assertions.assertTrue(command.runConstruction(player, label, new String[]{"A", "glass", "&cblock"}));
        player.assertSaid("Set item name: A glass §cblock");
        player.assertNoMoreSaid();

        item = player.getItemInHand();
        Assertions.assertEquals("A glass §cblock", item.getItemMeta().getDisplayName());
    }
}
