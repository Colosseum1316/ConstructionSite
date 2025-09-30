package colosseum.construction.test.dummies.data;

import colosseum.construction.data.MapDataImpl;
import colosseum.utility.WorldMapConstants;
import org.bukkit.World;

import java.io.File;

public final class DummyMapDataRead extends MapDataImpl {
    public DummyMapDataRead(World world, File worldFolder) {
        super(world, worldFolder);
    }

    @Override
    protected void init() {
        this.datFile = worldFolder.toPath().resolve(WorldMapConstants.MAP_DAT).toFile();
        super.read();
    }
}
