package colosseum.construction.test

import be.seeseemelk.mockbukkit.MockBukkit
import colosseum.construction.ConstructionSiteProvider
import colosseum.construction.manager.MapDataManager
import colosseum.construction.parser.MapParser
import colosseum.construction.test.dummies.ConstructionSiteServerMock
import colosseum.construction.test.dummies.ConstructionSiteWorldMock
import colosseum.construction.test.dummies.DummySite
import colosseum.construction.test.dummies.DummySite3
import colosseum.utility.WorldMapConstants
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

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
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
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
        }.get(90, TimeUnit.SECONDS))
    }
}