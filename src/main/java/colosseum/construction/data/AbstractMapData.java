package colosseum.construction.data;

import colosseum.utility.MapData;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public abstract class AbstractMapData implements MapData {
    @Nullable
    protected final World world;
    protected final File worldFolder;

    public AbstractMapData(@Nullable World world, File worldFolder) {
        this.world = world;
        this.worldFolder = worldFolder;
    }

    /**
     * Need to manually call this in subclass constructor.
     */
    protected abstract void init();

    protected abstract void read();
}
