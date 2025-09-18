package colosseum.construction.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import colosseum.construction.data.BlockData;
import colosseum.construction.test.dummies.DummySite;
import colosseum.construction.test.dummies.DummySite1;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

class TestBlockData {
    private static DummySite plugin;
    private static WorldMock world;

    @TempDir
    static File tempPluginDataDir;

    @BeforeAll
    static void setup() {
        plugin = new DummySite1(tempPluginDataDir);
        world = MockBukkit.getMock().addSimpleWorld("test");
        plugin.setup();
    }

    @AfterAll
    static void tearDown() {
        plugin.teardown();
        MockBukkit.unload();
    }

    @Test
    void testBlockData() {
        BlockMock blockMock = world.getBlockAt(0, 0, 0);
        blockMock.setType(Material.CHEST);
        blockMock.setData((byte) 1);
        BlockData blockData = new BlockData(blockMock);
        Assertions.assertEquals(blockMock, blockData.getBlock());
        Assertions.assertEquals(Material.CHEST, blockMock.getType());
        Assertions.assertEquals((byte) 1, blockMock.getData());
        Assertions.assertEquals(Material.CHEST, blockData.getMaterial());
        Assertions.assertEquals((byte) 1, blockData.getData());

        blockMock.setType(Material.LEAVES);
        blockMock.setData((byte) 2);
        Assertions.assertNotEquals(blockData.getMaterial(), blockMock.getType());
        Assertions.assertNotEquals(blockData.getData(), blockMock.getData());
        Assertions.assertEquals(Material.LEAVES, blockMock.getType());
        Assertions.assertEquals((byte) 2, blockMock.getData());
        Assertions.assertEquals(Material.CHEST, blockData.getMaterial());
        Assertions.assertEquals((byte) 1, blockData.getData());

        blockData.restore();
        Assertions.assertEquals(Material.CHEST, blockMock.getType());
        Assertions.assertEquals((byte) 1, blockMock.getData());
    }
}
