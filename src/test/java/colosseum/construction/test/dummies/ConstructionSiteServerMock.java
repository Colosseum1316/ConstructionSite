package colosseum.construction.test.dummies;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.logging.Level;

@SuppressWarnings("unchecked")
public final class ConstructionSiteServerMock extends ServerMock {

    public static ServerMock mock() {
        return Assertions.assertDoesNotThrow(() -> {
            Field field = MockBukkit.class.getDeclaredField("mock");
            field.setAccessible(true);
            ServerMock mock = (ServerMock) field.get(null);
            if (mock != null) {
                throw new IllegalStateException("Already mocking");
            }
            mock = new ConstructionSiteServerMock();
            Level defaultLevel = mock.getLogger().getLevel();
            mock.getLogger().setLevel(Level.WARNING);
            Bukkit.setServer(mock);
            mock.getLogger().setLevel(defaultLevel);
            field.set(null, mock);
            return mock;
        });
    }

    public ArrayList<World> getWorldsMutable() {
        return Assertions.assertDoesNotThrow(() -> {
            Field field = ServerMock.class.getDeclaredField("worlds");
            field.setAccessible(true);
            return (ArrayList<World>) field.get(this);
        });
    }

    @Override
    public WorldMock addSimpleWorld(String name) {
        ConstructionSiteWorldMock mock = new ConstructionSiteWorldMock(name);
        return addWorld(mock);
    }

    public WorldMock addWorld(ConstructionSiteWorldMock worldMock) {
        getWorldsMutable().add(worldMock);
        return worldMock;
    }

    public void addPlayer(ConstructionSitePlayerMock playerMock) {
        super.addPlayer(playerMock);
    }

    @Override
    public boolean unloadWorld(String name, boolean save) {
        return unloadWorld(getWorld(name), save);
    }

    @Override
    public boolean unloadWorld(World world, boolean save) {
        return Assertions.assertDoesNotThrow(() -> {
            getWorldsMutable().remove(world);
            return true;
        });
    }

    @Override
    public World createWorld(WorldCreator creator) {
        return getWorld(creator.name());
    }
}
