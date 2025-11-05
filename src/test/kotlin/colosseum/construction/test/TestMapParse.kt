package colosseum.construction.test

import be.seeseemelk.mockbukkit.MockBukkit
import colosseum.construction.Constants
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.manager.MapDataManager
import colosseum.construction.parser.MapParser
import colosseum.construction.test.dummies.ConstructionSiteServerMock
import colosseum.construction.test.dummies.ConstructionSiteWorldMock
import colosseum.construction.test.dummies.DummySite
import colosseum.construction.test.dummies.DummySite3
import colosseum.utility.WorldMapConstants
import org.bukkit.Material
import org.bukkit.util.Vector
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*

internal class TestMapParse {
    companion object {
        @TempDir
        @JvmField
        var tempWorldContainer: File? = null

        @TempDir
        @JvmField
        var tempPluginDataDir: File? = null
    }

    private var plugin: DummySite? = null
    private lateinit var world: ConstructionSiteWorldMock

    @BeforeAll
    fun setup() {
        tearDown()
        plugin = DummySite3(tempWorldContainer, tempPluginDataDir)
        world = ConstructionSiteWorldMock(WorldMapConstants.WORLD)
        (MockBukkit.getMock() as ConstructionSiteServerMock).addWorld(world)
        Assertions.assertEquals(world, MockBukkit.getMock().getWorld(WorldMapConstants.WORLD))
        plugin!!.load()
        plugin!!.enable()
    }

    @AfterAll
    fun tearDown() {
        Utils.tearDown(plugin)
    }

    @Test
    fun testInputs() {
        val destination = ResourceSession.path.resolve("Void").toFile()
        val mapData = ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java).getFinalized(destination)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            MapParser(destination, mapData, Collections.emptyList(), 0, 0, -1)
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun testCancellation() {
        val destination = ResourceSession.path.resolve("Void").toFile()
        val mapData = ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java).getFinalized(destination)
        val parser = MapParser(destination, mapData, Collections.emptyList(), 0, 0, 1000)
        val v = AtomicBoolean(false)
        CompletableFuture.runAsync(parser).thenRun {
            Assertions.assertFalse(parser.isRunning)
            Assertions.assertFalse(parser.isSuccess)
            Assertions.assertTrue(parser.isCancelled)
            Assertions.assertTrue(parser.isFail)
            v.set(true)
        }
        Thread.sleep(1000)
        parser.cancel()
        Thread.sleep(1000)
        Assertions.assertTrue(v.get())
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    fun testHerosValley() {
        val destination = ResourceSession.path.resolve("Heros_Valley").toFile()
        val mapData = ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java).getFinalized(destination)
        val parser = MapParser(destination, mapData, Collections.emptyList(), 0, 0, 150)
        Assertions.assertEquals(mapData, parser.mapData)
        Assertions.assertEquals(destination, parser.parsableWorldFolder)
        Assertions.assertTrue(CompletableFuture.supplyAsync {
            parser.run()
            Assertions.assertFalse(parser.isFail)
            Assertions.assertFalse(parser.isCancelled)
            return@supplyAsync parser.isSuccess
        }.get(120, TimeUnit.SECONDS))

        Assertions.assertDoesNotThrow {
            val dat = Files.readAllLines(destination.toPath().resolve(WorldMapConstants.WORLDCONFIG_DAT))
            var mapName = false
            var mapAuthor = false
            var minX = false
            var minY = false
            var minZ = false
            var maxX = false
            var maxY = false
            var maxZ = false

            val teams = HashMap<String, ArrayList<Vector>>()
            val customs = HashMap<String, ArrayList<Vector>>()
            val datas = HashMap<String, ArrayList<Vector>>()

            var tn = ""
            var dn = ""
            var dataN = ""
            var tn_f = false
            var dn_f = false
            var data_f = false

            for (line in dat) {
                val tokens = line.split(":")
                if (tokens.size != 2) {
                    continue
                }
                val key = tokens[0]
                val value = tokens[1]

                if (dn_f || tn_f || data_f) {
                    val countFlags = listOf(dn_f, tn_f, data_f).count { it }
                    Assertions.assertEquals(1, countFlags)
                }

                if (tn_f) {
                    Assertions.assertEquals("TEAM_SPAWNS", key)
                    val entries = value.split(Constants.LOCATIONS_DELIMITER)
                    Assertions.assertTrue(entries.isNotEmpty())
                    for (e in entries) {
                        val xyz = e.split(",").map { it.toInt() }
                        teams.computeIfAbsent(tn) { ArrayList() }.add(Vector(xyz[0], xyz[1], xyz[2]))
                    }
                    tn = ""
                    tn_f = false
                    continue
                }

                if (dn_f) {
                    Assertions.assertEquals("CUSTOM_LOCS", key)
                    val entries = value.split(Constants.LOCATIONS_DELIMITER)
                    Assertions.assertTrue(entries.isNotEmpty())
                    for (e in entries) {
                        val xyz = e.split(",").map { it.toInt() }
                        customs.computeIfAbsent(dn) { ArrayList() }.add(Vector(xyz[0], xyz[1], xyz[2]))
                    }
                    dn = ""
                    dn_f = false
                    continue
                }

                if (data_f) {
                    Assertions.assertEquals("DATA_LOCS", key)
                    val entries = value.split(Constants.LOCATIONS_DELIMITER)
                    Assertions.assertTrue(entries.isNotEmpty())
                    for (e in entries) {
                        val xyz = e.split(",").map { it.toInt() }
                        datas.computeIfAbsent(dataN) { ArrayList() }.add(Vector(xyz[0], xyz[1], xyz[2]))
                    }
                    dataN = ""
                    data_f = false
                    continue
                }

                when (key) {
                    "MAP_NAME" -> {
                        Assertions.assertEquals("Heros Valley", value)
                        mapName = true
                    }
                    "MAP_AUTHOR" -> {
                        Assertions.assertEquals("Dutty", value)
                        mapAuthor = true
                    }
                    "MIN_X" -> {
                        Assertions.assertEquals(-65, value.toInt())
                        minX = true
                    }
                    "MIN_Y" -> {
                        Assertions.assertEquals(19, value.toInt())
                        minY = true
                    }
                    "MIN_Z" -> {
                        Assertions.assertEquals(-121, value.toInt())
                        minZ = true
                    }
                    "MAX_X" -> {
                        Assertions.assertEquals(67, value.toInt())
                        maxX = true
                    }
                    "MAX_Y" -> {
                        Assertions.assertEquals(76, value.toInt())
                        maxY = true
                    }
                    "MAX_Z" -> {
                        Assertions.assertEquals(123, value.toInt())
                        maxZ = true
                    }
                    "TEAM_NAME" -> {
                        tn = value
                        tn_f = true
                    }
                    "CUSTOM_NAME" -> {
                        dn = value
                        dn_f = true
                    }
                    "DATA_NAME" -> {
                        dataN = value
                        data_f = true
                    }
                    else -> Assertions.fail("Invalid key $key")
                }
            }

            Assertions.assertTrue(minX)
            Assertions.assertTrue(minY)
            Assertions.assertTrue(minZ)
            Assertions.assertTrue(maxX)
            Assertions.assertTrue(maxY)
            Assertions.assertTrue(maxZ)
            Assertions.assertTrue(mapName)
            Assertions.assertTrue(mapAuthor)

            Assertions.assertEquals(2, teams.size)
            Assertions.assertEquals(9, datas.size)
            Assertions.assertEquals(19, customs.size)

            Assertions.assertTrue(teams.containsKey("Red"))
            Assertions.assertTrue(teams.containsKey("Blue"))

            var t = teams.get("Red")
            Assertions.assertEquals(4, t!!.size)
            Assertions.assertNotNull(t.find { v -> v.x == -11.0 && v.y == 32.0 && v.z == 90.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -9.0 && v.y == 32.0 && v.z == 90.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -7.0 && v.y == 32.0 && v.z == 90.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -5.0 && v.y == 32.0 && v.z == 90.0 })

            t = teams.get("Blue")
            Assertions.assertEquals(4, t!!.size)
            Assertions.assertNotNull(t.find { v -> v.x == 5.0 && v.y == 32.0 && v.z == -91.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 7.0 && v.y == 32.0 && v.z == -91.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 9.0 && v.y == 32.0 && v.z == -91.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 11.0 && v.y == 32.0 && v.z == -91.0 })

            var d = datas.get("RED")
            Assertions.assertEquals(1, d!!.size)
            Assertions.assertNotNull(d.find { v -> v.x == -8.0 && v.y == 32.0 && v.z == 77.0 })

            d = datas.get("GRAY")
            Assertions.assertEquals(13, d!!.size)
            Assertions.assertNotNull(d.find { v -> v.x == -9.0 && v.y == 26.0 && v.z == 7.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -8.0 && v.y == 26.0 && v.z == 8.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -7.0 && v.y == 26.0 && v.z == 9.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -6.0 && v.y == 26.0 && v.z == 9.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -5.0 && v.y == 26.0 && v.z == 10.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -4.0 && v.y == 26.0 && v.z == 10.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 4.0 && v.y == 26.0 && v.z == -11.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 5.0 && v.y == 26.0 && v.z == -11.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 6.0 && v.y == 26.0 && v.z == -10.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 7.0 && v.y == 26.0 && v.z == -10.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 8.0 && v.y == 26.0 && v.z == -9.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 9.0 && v.y == 26.0 && v.z == -8.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 10.0 && v.y == 26.0 && v.z == -7.0 })

            d = datas.get("WHITE")
            Assertions.assertEquals(1, d!!.size)
            Assertions.assertNotNull(d.find { v -> v.x == 8.0 && v.y == 32.0 && v.z == -71.0 })

            d = datas.get("LIGHT_BLUE")
            Assertions.assertEquals(23, d!!.size)
            Assertions.assertNotNull(d.find { v -> v.x == -14.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -12.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -10.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -8.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -6.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -4.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -2.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 0.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 2.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 4.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 6.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 8.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 10.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 12.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 14.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 16.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 18.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 20.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 22.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 24.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 26.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 28.0 && v.y == 32.0 && v.z == -81.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 30.0 && v.y == 32.0 && v.z == -81.0 })

            d = datas.get("BLUE")
            Assertions.assertEquals(1, d!!.size)
            Assertions.assertNotNull(d.find { v -> v.x == 8.0 && v.y == 32.0 && v.z == -78.0 })

            d = datas.get("SILVER")
            Assertions.assertEquals(2, d!!.size)
            Assertions.assertNotNull(d.find { v -> v.x == -8.0 && v.y == 36.0 && v.z == 100.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 8.0 && v.y == 36.0 && v.z == -101.0 })

            d = datas.get("BLACK")
            Assertions.assertEquals(1, d!!.size)
            Assertions.assertNotNull(d.find { v -> v.x == 0.0 && v.y == 25.0 && v.z == 0.0 })

            d = datas.get("BROWN")
            Assertions.assertEquals(17, d!!.size)
            Assertions.assertNotNull(d.find { v -> v.x == -16.0 && v.y == 32.0 && v.z == -21.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -12.0 && v.y == 32.0 && v.z == -29.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -11.0 && v.y == 32.0 && v.z == -12.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -8.0 && v.y == 32.0 && v.z == -34.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -8.0 && v.y == 32.0 && v.z == 60.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -8.0 && v.y == 32.0 && v.z == 66.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -5.0 && v.y == 32.0 && v.z == -5.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -5.0 && v.y == 32.0 && v.z == 53.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -1.0 && v.y == 32.0 && v.z == 43.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 1.0 && v.y == 32.0 && v.z == 1.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 3.0 && v.y == 32.0 && v.z == 38.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 6.0 && v.y == 32.0 && v.z == 7.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 8.0 && v.y == 32.0 && v.z == -59.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 8.0 && v.y == 32.0 && v.z == 33.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 11.0 && v.y == 32.0 && v.z == 12.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 12.0 && v.y == 32.0 && v.z == 29.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 16.0 && v.y == 32.0 && v.z == 21.0 })

            d = datas.get("ORANGE")
            Assertions.assertEquals(22, d!!.size)
            Assertions.assertNotNull(d.find { v -> v.x == -30.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -28.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -26.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -24.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -22.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -20.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -18.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -16.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -14.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -12.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -10.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -8.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -6.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -4.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == -2.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 0.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 2.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 4.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 8.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 10.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 12.0 && v.y == 32.0 && v.z == 80.0 })
            Assertions.assertNotNull(d.find { v -> v.x == 14.0 && v.y == 32.0 && v.z == 80.0 })

            fun assertCustom(name: String, x: Int, y: Int, z: Int) {
                val c = customs.get(name)
                Assertions.assertEquals(1, c!!.size)
                Assertions.assertNotNull(c.find { v -> v.x == x.toDouble() && v.y == y.toDouble() && v.z == z.toDouble() })
            }

            assertCustom("KIT RED ASSASSIN", -13, 32, 85)
            assertCustom("TOWER BLUE 2", 8, 40, -51)
            assertCustom("TOWER BLUE 1", -10, 40, -21)
            assertCustom("KIT BLUE GO_BACK", 8, 32, -70)
            assertCustom("KIT BLUE HUNTER", 10, 32, -84)
            assertCustom("KIT RED MAGE", -6, 32, 83)
            assertCustom("KIT RED WARRIOR", -3, 32, 85)
            assertCustom("TOWER RED 2", -8, 40, 50)
            assertCustom("EMBLEM BLUE", -10, 32, -87)
            assertCustom("TOWER RED 1", 10, 40, 21)
            assertCustom("KIT RED HUNTER", -10, 32, 83)
            assertCustom("POINT Gold GOLD", 19, 26, -20)
            assertCustom("CENTER", 8, 37, -1)
            assertCustom("POINT Emerald GREEN", -19, 26, 19)
            assertCustom("KIT BLUE ASSASSIN", 13, 32, -86)
            assertCustom("KIT BLUE WARRIOR", 3, 32, -86)
            assertCustom("KIT BLUE MAGE", 6, 32, -84)
            assertCustom("EMBLEM RED", 10, 32, 86)
            assertCustom("KIT RED GO_BACK", -8, 32, 69)
        }
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    fun testCliffside() {
        val destination = ResourceSession.path.resolve("Cliffside").toFile()
        val mapData = ConstructionSiteProvider.getSite().getManager(MapDataManager::class.java).getFinalized(destination)
        val parser = MapParser(destination, mapData, listOf(Material.GLASS.id.toString()), 0, 0, 50)
        Assertions.assertEquals(mapData, parser.mapData)
        Assertions.assertEquals(destination, parser.parsableWorldFolder)
        Assertions.assertTrue(CompletableFuture.supplyAsync {
            parser.run()
            Assertions.assertFalse(parser.isFail)
            Assertions.assertFalse(parser.isCancelled)
            return@supplyAsync parser.isSuccess
        }.get(60, TimeUnit.SECONDS))

        Assertions.assertDoesNotThrow {
            val dat = Files.readAllLines(destination.toPath().resolve(WorldMapConstants.WORLDCONFIG_DAT))
            var mapName = false
            var mapAuthor = false
            var minX = false
            var minY = false
            var minZ = false
            var maxX = false
            var maxY = false
            var maxZ = false
            val teams = HashMap<String, ArrayList<Vector>>()
            val customs = HashMap<String, ArrayList<Vector>>()
            var tn = ""
            var dn = ""
            var tn_f = false
            var dn_f = false
            for (line in dat) {
                val tokens = line.split(":")
                if (tokens.size != 2) {
                    continue
                }
                val key = tokens[0]
                val value = tokens[1]
                if (dn_f || tn_f) {
                    Assertions.assertTrue(dn_f.xor(tn_f))
                }
                if (tn_f) {
                    Assertions.assertEquals("TEAM_SPAWNS", key)
                    val entries = value.split(Constants.LOCATIONS_DELIMITER)
                    Assertions.assertTrue(entries.isNotEmpty())
                    for (e in entries) {
                        val xyz = e.split(",").map { it.toInt() }
                        teams.computeIfAbsent(tn) { ArrayList() }.add(Vector(xyz[0], xyz[1], xyz[2]))
                    }
                    tn = ""
                    tn_f = false
                    continue
                }
                if (dn_f) {
                    Assertions.assertEquals("CUSTOM_LOCS", key)
                    val entries = value.split(Constants.LOCATIONS_DELIMITER)
                    Assertions.assertTrue(entries.isNotEmpty())
                    for (e in entries) {
                        val xyz = e.split(",").map { it.toInt() }
                        customs.computeIfAbsent(dn) { ArrayList() }.add(Vector(xyz[0], xyz[1], xyz[2]))
                    }
                    dn = ""
                    dn_f = false
                    continue
                }
                when (key) {
                    "MAP_NAME" -> {
                        Assertions.assertEquals("Cliffside", value)
                        mapName = true
                    }
                    "MAP_AUTHOR" -> {
                        Assertions.assertEquals("Pyxl", value)
                        mapAuthor = true
                    }
                    "MIN_X" -> {
                        Assertions.assertEquals(-39, value.toInt())
                        minX = true
                    }
                    "MIN_Y" -> {
                        Assertions.assertEquals(1, value.toInt())
                        minY = true
                    }
                    "MIN_Z" -> {
                        Assertions.assertEquals(-38, value.toInt())
                        minZ = true
                    }
                    "MAX_X" -> {
                        Assertions.assertEquals(37, value.toInt())
                        maxX = true
                    }
                    "MAX_Y" -> {
                        Assertions.assertEquals(47, value.toInt())
                        maxY = true
                    }
                    "MAX_Z" -> {
                        Assertions.assertEquals(39, value.toInt())
                        maxZ = true
                    }
                    "TEAM_NAME" -> {
                        tn = value
                        tn_f = true
                    }
                    "CUSTOM_NAME" -> {
                        dn = value
                        dn_f = true
                    }
                    else -> Assertions.fail("Invalid key $key")
                }
            }
            Assertions.assertTrue(minX)
            Assertions.assertTrue(minY)
            Assertions.assertTrue(minZ)
            Assertions.assertTrue(maxX)
            Assertions.assertTrue(maxY)
            Assertions.assertTrue(maxZ)
            Assertions.assertTrue(mapName)
            Assertions.assertTrue(mapAuthor)
            Assertions.assertEquals(4, teams.size)
            Assertions.assertEquals(1, customs.size)
            Assertions.assertTrue(teams.containsKey("Red"))
            Assertions.assertTrue(teams.containsKey("Blue"))
            Assertions.assertTrue(teams.containsKey("Yellow"))
            Assertions.assertTrue(teams.containsKey("Green"))
            Assertions.assertTrue(customs.containsKey("20"))

            var t = teams.get("Red")
            Assertions.assertEquals(13, t!!.size)
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == -22.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == -19.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == -16.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == -13.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == -10.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == -7.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 7.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 10.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 13.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 16.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 19.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 22.0 && v.y == 15.0 && v.z == -4.0 })

            t = teams.get("Blue")
            Assertions.assertEquals(13, t!!.size)
            Assertions.assertNotNull(t.find { v -> v.x == -22.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -19.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -16.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -13.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -10.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -7.0 && v.y == 15.0 && v.z == -4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == -22.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == -19.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == -16.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == -13.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == -10.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == -7.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == -4.0 })

            t = teams.get("Yellow")
            Assertions.assertEquals(14, t!!.size)
            Assertions.assertNotNull(t.find { v -> v.x == -22.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -19.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -16.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -13.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -10.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -7.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == 7.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == 10.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == 13.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == 16.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == 19.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == 22.0 })
            Assertions.assertNotNull(t.find { v -> v.x == -4.0 && v.y == 15.0 && v.z == 25.0 })

            t = teams.get("Green")
            Assertions.assertEquals(14, t!!.size)
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == 7.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == 10.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == 13.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == 16.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == 19.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == 22.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 4.0 && v.y == 15.0 && v.z == 25.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 7.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 10.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 13.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 16.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 19.0 && v.y == 15.0 && v.z == 4.0 })
            Assertions.assertNotNull(t.find { v -> v.x == 22.0 && v.y == 15.0 && v.z == 4.0 })

            val c = customs.get("20")
            fun assertHas(x: Int, y: Int, z: Int) {
                Assertions.assertNotNull(c!!.find { v -> v.x == x.toDouble() && v.y == y.toDouble() && v.z == z.toDouble() })
            }

            for (x in -34..-1) {
                for (y in 15..19) {
                    assertHas(x, y, 0)
                }
            }

            for (z in -34..-1) {
                for (y in 15..19) {
                    assertHas(0, y, z)
                }
            }

            for (z in 1..34) {
                for (y in 15..19) {
                    assertHas(0, y, z)
                }
            }

            for (x in 1..34) {
                for (y in 15..19) {
                    assertHas(x, y, 0)
                }
            }
        }
    }
}