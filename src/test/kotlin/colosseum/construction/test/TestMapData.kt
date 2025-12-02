package colosseum.construction.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.WorldMock
import be.seeseemelk.mockbukkit.entity.PlayerMock
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.WorldUtils
import colosseum.construction.data.DummyMapData
import colosseum.construction.data.FinalizedMapData
import colosseum.construction.data.MapData
import colosseum.construction.data.MapDataImpl
import colosseum.construction.test.dummies.DummySite
import colosseum.construction.test.dummies.DummySite1
import colosseum.construction.test.dummies.data.DummyMapDataRead
import colosseum.construction.test.dummies.data.DummyMapDataWrite
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import org.apache.commons.io.FileUtils
import org.bukkit.util.Vector
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.*

internal class TestMapData {
    
    companion object {
        @TempDir
        @JvmField
        var tempWorldDir: File? = null

        @TempDir
        @JvmField
        var tempPluginDataDir: File? = null

        private const val uuid1 = "5da001d1-f9a4-4c95-9736-9a98327848bf"
        private const val uuid2 = "07e79d0b-f86d-4bed-ae37-d87df8d94693"
        private const val uuid3 = "3e65ea50-cd1a-45fb-81d7-7e27c14662d4"
    }
    
    private var plugin: DummySite? = null
    private lateinit var world: WorldMock

    private lateinit var player1: PlayerMock
    private lateinit var player2: PlayerMock
    private lateinit var player3: PlayerMock

    @BeforeAll
    fun setup() {
        tearDown()
        plugin = DummySite1(tempPluginDataDir)
        world = MockBukkit.getMock().addSimpleWorld("test_map")
        player1 = PlayerMock("test1", UUID.fromString(uuid1))
        player1.isOp = false
        MockBukkit.getMock().addPlayer(player1)
        player2 = PlayerMock("test2", UUID.fromString(uuid2))
        player2.isOp = false
        MockBukkit.getMock().addPlayer(player2)
        player3 = PlayerMock("test3", UUID.fromString(uuid3))
        player3.isOp = true
        MockBukkit.getMock().addPlayer(player3)
        plugin!!.enable()
    }

    @AfterAll
    fun tearDown() {
        Utils.tearDown(plugin)
    }

    @BeforeEach
    fun setupEach() {
        val file: File = WorldUtils.mapDatFile(tempWorldDir!!)
        ConstructionSiteProvider.getSite().getPluginLogger().info("Deleting " + file.absolutePath)
        FileUtils.deleteQuietly(file)
    }

    private fun testRead0(testCase: String): DummyMapDataRead {
        return Utils.readMapData(world, tempWorldDir, testCase)
    }

    private fun interface DummyMapWriteAssertionCallback {
        fun assertion(
            data: MapData,
            mapName: String,
            mapCreator: String,
            warps: MutableMap<String, Vector>,
            adminList: MutableSet<UUID>,
            currentlyLive: Boolean
        )
    }

    private fun testWrite0(
        mapName: String,
        mapCreator: String,
        warps: MutableMap<String, Vector>,
        adminList: MutableSet<UUID>,
        currentlyLive: Boolean,
        assertion: DummyMapWriteAssertionCallback
    ) {
        DummyMapDataWrite(
            world,
            tempWorldDir,
            mapName,
            mapCreator,
            warps,
            adminList,
            currentlyLive
        ).write()
        val data: MapData = Utils.readMapData(world, tempWorldDir)
        assertion.assertion(data, mapName, mapCreator, warps, adminList, currentlyLive)
    }

    @Order(1)
    @Test
    fun testRead() {
        var data: MapData = testRead0(
            String.format(
                """
                
                currentlyLive:true
                warps:w1@(0,0,0);w2@(1,0,-1);w3@-1,-1,1;w4w4@0,0,0;
                MAP_NAME:Test Map_-Name123 456 789
                MAP_AUTHOR: Test Author_-Team123 456 789
                ADMIN_LIST:%s,%s
                """.trimIndent().trim { it <= ' ' }, uuid1, uuid2
            )
        )
        Assertions.assertTrue(data.isLive)
        Assertions.assertEquals(4, data.warps().size)
        Assertions.assertTrue(data.warps().containsKey("w1"))
        Assertions.assertEquals(data.warps().get("w1"), Vector(0, 0, 0))
        Assertions.assertTrue(data.warps().containsKey("w2"))
        Assertions.assertEquals(data.warps().get("w2"), Vector(1, 0, -1))
        Assertions.assertTrue(data.warps().containsKey("w3"))
        Assertions.assertEquals(data.warps().get("w3"), Vector(-1, -1, 1))
        Assertions.assertTrue(data.warps().containsKey("w4w4"))
        Assertions.assertEquals(data.warps().get("w4w4"), Vector(0, 0, 0))
        Assertions.assertEquals("Test Map_-Name123 456 789", data.mapName)
        Assertions.assertEquals(" Test Author_-Team123 456 789", data.mapCreator)
        Assertions.assertEquals(2, data.adminList().size)
        Assertions.assertTrue(
            data.adminList().stream().anyMatch({ v -> v.toString() == uuid1 })
        )
        Assertions.assertTrue(
            data.adminList().stream().anyMatch({ v -> v.toString() == uuid2 })
        )
        Assertions.assertTrue(data.allows(player1))
        Assertions.assertTrue(data.allows(player2))
        Assertions.assertTrue(data.allows(player3))

        data = testRead0(
            String.format(
                """
                :invalid
                currentlyLive:false
                warps:;
                MAP_NAME:TEST MAP
                MAP_AUTHOR:TEST AUTHOR
                ADMIN_LIST:%s
                """.trimIndent().trim { it <= ' ' }, uuid1
            )
        )
        Assertions.assertFalse(data.isLive)
        Assertions.assertTrue(data.warps().isEmpty())
        Assertions.assertEquals("TEST MAP", data.getMapName())
        Assertions.assertEquals("TEST AUTHOR", data.getMapCreator())
        Assertions.assertEquals(1, data.adminList().size)
        Assertions.assertTrue(
            data.adminList().stream().anyMatch({ v -> v.toString() == uuid1 })
        )
        Assertions.assertFalse(
            data.adminList().stream().anyMatch({ v -> v.toString() == uuid2 })
        )
        Assertions.assertTrue(data.allows(player1))
        Assertions.assertFalse(data.allows(player2))
        Assertions.assertTrue(data.allows(player3))
    }

    @Order(2)
    @Test
    fun testWrite() {
        testWrite0(
            "TEST MAP NONETYPE",
            "TEST MAP NONETYPE AUTHOR",
            kotlin.collections.mutableMapOf<String, Vector>(),
            kotlin.collections.mutableSetOf<UUID>(),
            true
        ) { data: MapData, mapName: String, mapCreator: String, warps: MutableMap<String, Vector>, adminList: MutableSet<UUID>, currentlyLive: Boolean ->
            Assertions.assertTrue(data.isLive)
            Assertions.assertTrue(data.warps().isEmpty())
            Assertions.assertTrue(data.adminList().isEmpty())
            Assertions.assertEquals("TEST MAP NONETYPE", data.mapName)
            Assertions.assertEquals("TEST MAP NONETYPE AUTHOR", data.mapCreator)
        }
        testWrite0(
            "TEST MAP 1",
            "TEST MAP 1 AUTHOR",
            mutableMapOf(
                "a1" to Vector(0, 0, 0),
                "a2" to Vector(0, 1, 0),
                "a3" to Vector(-1, 0, 1)
            ),
            mutableSetOf(
                UUID.fromString(uuid1),
                UUID.fromString(uuid2)
            ),
            false
        ) { data: MapData, mapName: String, mapCreator: String, warps: MutableMap<String, Vector>, adminList: MutableSet<UUID>, currentlyLive: Boolean ->
            Assertions.assertFalse(data.isLive)
            Assertions.assertEquals(3, data.warps().size)
            Assertions.assertTrue(data.warps().containsKey("a1"))
            Assertions.assertEquals(data.warps().get("a1"), Vector(0, 0, 0))
            Assertions.assertTrue(data.warps().containsKey("a2"))
            Assertions.assertEquals(data.warps().get("a2"), Vector(0, 1, 0))
            Assertions.assertTrue(data.warps().containsKey("a3"))
            Assertions.assertEquals(data.warps().get("a3"), Vector(-1, 0, 1))
            Assertions.assertEquals(2, data.adminList().size)
            Assertions.assertTrue(
                data.adminList().stream().anyMatch({ v -> v.toString() == uuid1 })
            )
            Assertions.assertTrue(
                data.adminList().stream().anyMatch({ v -> v.toString() == uuid2 })
            )
            Assertions.assertEquals("TEST MAP 1", data.mapName)
            Assertions.assertEquals("TEST MAP 1 AUTHOR", data.mapCreator)
            Assertions.assertTrue(data.allows(player1))
            Assertions.assertTrue(data.allows(player2))
            Assertions.assertTrue(data.allows(player3))
        }
    }

    @Test
    fun assertDummies() {
        val data: MapData = DummyMapData()
        val player: PlayerMock = MockBukkit.getMock().addPlayer()
        Assertions.assertTrue(data.isLive)
        Assertions.assertEquals(0, data.warps().size)
        Assertions.assertEquals(0, data.adminList().size)
        Assertions.assertTrue(data.allows(player))
    }

    @Test
    fun assertInitialization() {
        Assertions.assertFalse(WorldUtils.mapDatFile(tempWorldDir!!).exists())
        val data: MapData = MapDataImpl(null, tempWorldDir!!)
        Assertions.assertTrue(WorldUtils.mapDatFile(tempWorldDir!!).exists())
        Assertions.assertEquals("MapName", data.mapName)
        Assertions.assertEquals("MapCreator", data.mapCreator)
        Assertions.assertEquals(0, data.warps().size)
        Assertions.assertEquals(0, data.adminList().size)
        Assertions.assertTrue(data.isLive)
    }

    @Test
    fun assertFinalized() {
        val mapName = "TEST MAP FINALIZED"
        val mapCreator = "TEST MAP AUTHOR FINALIZED"
        val warps: ImmutableMap<String, Vector> = ImmutableMap.of(
            "a1", Vector(0, 0, 0),
            "a2", Vector(0, 1, 0),
            "a3", Vector(-1, 0, 1)
        )
        val adminList: ImmutableSet<UUID> = ImmutableSet.of(
            UUID.fromString(uuid1),
            UUID.fromString(uuid2),
            UUID.fromString(uuid3)
        )
        var mapData = FinalizedMapData(
            mapName, mapCreator, warps, adminList, false
        )
        Assertions.assertEquals(mapName, mapData.mapName.get())
        Assertions.assertEquals(mapCreator, mapData.mapCreator.get())
        Assertions.assertEquals(warps, mapData.warps.get())
        Assertions.assertEquals(adminList, mapData.adminList.get())
        Assertions.assertFalse(mapData.live.get())

        mapData = FinalizedMapData(
            mapName, mapCreator
        )
        Assertions.assertEquals(mapName, mapData.mapName.get())
        Assertions.assertEquals(mapCreator, mapData.mapCreator.get())
        Assertions.assertNull(mapData.warps.orElse(null))
        Assertions.assertNull(mapData.adminList.orElse(null))
        Assertions.assertNull(mapData.live.orElse(null))

        mapData = FinalizedMapData(
            mapName, mapCreator, warps
        )
        Assertions.assertEquals(mapName, mapData.mapName.get())
        Assertions.assertEquals(mapCreator, mapData.mapCreator.get())
        Assertions.assertEquals(warps, mapData.warps.get())
        Assertions.assertNull(mapData.adminList.orElse(null))
        Assertions.assertNull(mapData.live.orElse(null))

        mapData = FinalizedMapData(mapName, mapCreator, adminList)
        Assertions.assertEquals(mapName, mapData.mapName.get())
        Assertions.assertEquals(mapCreator, mapData.mapCreator.get())
        Assertions.assertNull(mapData.warps.orElse(null))
        Assertions.assertEquals(adminList, mapData.adminList.get())
        Assertions.assertNull(mapData.live.orElse(null))

        mapData = FinalizedMapData(true)
        Assertions.assertNull(mapData.mapName.orElse(null))
        Assertions.assertNull(mapData.mapCreator.orElse(null))
        Assertions.assertNull(mapData.warps.orElse(null))
        Assertions.assertNull(mapData.adminList.orElse(null))
        Assertions.assertTrue(mapData.live.get())

        mapData = FinalizedMapData(adminList)
        Assertions.assertNull(mapData.mapName.orElse(null))
        Assertions.assertNull(mapData.mapCreator.orElse(null))
        Assertions.assertNull(mapData.warps.orElse(null))
        Assertions.assertEquals(adminList, mapData.adminList.get())
        Assertions.assertNull(mapData.live.orElse(null))

        mapData = FinalizedMapData(warps)
        Assertions.assertNull(mapData.mapName.orElse(null))
        Assertions.assertNull(mapData.mapCreator.orElse(null))
        Assertions.assertEquals(warps, mapData.warps.get())
        Assertions.assertNull(mapData.adminList.orElse(null))
        Assertions.assertNull(mapData.live.orElse(null))

        mapData = FinalizedMapData(
            testRead0(
                String.format(
                    """
                currentlyLive:true
                warps:w1@(0,0,0);w2@(1,0,-1);w3@-1,-1,1;w4w4@0,0,0;
                MAP_NAME:Test Map finalized 2
                MAP_AUTHOR: Test Author finalized 2
                ADMIN_LIST:%s,%s
                """.trimIndent().trim { it <= ' ' }, uuid1, uuid2
                )
            )
        )
        Assertions.assertTrue(mapData.live.orElse(false))
        Assertions.assertEquals(4, mapData.warps.orElse(ImmutableMap.of()).size)
        Assertions.assertTrue(mapData.warps.get().containsKey("w1"))
        Assertions.assertEquals(mapData.warps.get().get("w1"), Vector(0, 0, 0))
        Assertions.assertTrue(mapData.warps.get().containsKey("w2"))
        Assertions.assertEquals(mapData.warps.get().get("w2"), Vector(1, 0, -1))
        Assertions.assertTrue(mapData.warps.get().containsKey("w3"))
        Assertions.assertEquals(mapData.warps.get().get("w3"), Vector(-1, -1, 1))
        Assertions.assertTrue(mapData.warps.get().containsKey("w4w4"))
        Assertions.assertEquals(mapData.warps.get().get("w4w4"), Vector(0, 0, 0))
        Assertions.assertEquals("Test Map finalized 2", mapData.mapName.orElse(null))
        Assertions.assertEquals(" Test Author finalized 2", mapData.mapCreator.orElse(null))
        Assertions.assertEquals(2, mapData.adminList.orElse(ImmutableSet.of()).size)
        Assertions.assertTrue(
            mapData.adminList.get().stream().anyMatch({ v -> v.toString() == uuid1 })
        )
        Assertions.assertTrue(
            mapData.adminList.get().stream().anyMatch({ v -> v.toString() == uuid2 })
        )
    }
}
