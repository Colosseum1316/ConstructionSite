package colosseum.construction.test.dummies.data;

import colosseum.construction.WorldUtils;
import colosseum.construction.data.MapDataImpl;
import org.bukkit.World;

import java.io.File;

public final class DummyMapDataRead extends MapDataImpl {
    public DummyMapDataRead(World world, File worldFolder) {
        super(world, worldFolder);
    }

    @Override
    protected void init() {
        this.datFile = WorldUtils.mapDatFile(worldFolder);
        super.read();
    }
}
