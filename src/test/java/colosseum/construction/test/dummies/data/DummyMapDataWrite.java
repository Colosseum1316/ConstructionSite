package colosseum.construction.test.dummies.data;

import colosseum.construction.data.MapDataImpl;
import colosseum.utility.WorldMapConstants;
import colosseum.utility.arcade.GameType;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DummyMapDataWrite extends MapDataImpl {
    private final boolean testCurrentlyLive;
    private final Map<String, Vector> testWarps;
    private final Set<UUID> testAdminList;
    private final GameType testMapGameType;
    private final String testMapName;
    private final String testMapCreator;

    public DummyMapDataWrite(
            World world,
            File worldFolder,
            boolean currentlyLive,
            @NonNull Map<String, Vector> warps,
            @NonNull Set<UUID> adminList,
            @NonNull GameType mapGameType,
            @NonNull String mapName,
            @NonNull String mapCreator
    ) {
        super(world, worldFolder);
        this.testCurrentlyLive = currentlyLive;
        this.testWarps = warps;
        this.testAdminList = adminList;
        this.testMapGameType = mapGameType;
        this.testMapName = mapName;
        this.testMapCreator = mapCreator;
        super.setLive(this.testCurrentlyLive);
        super.getWarps().putAll(this.testWarps);
        super.getAdminList().addAll(this.testAdminList);
        super.setMapGameType(this.testMapGameType);
        super.setMapName(this.testMapName);
        super.setMapCreator(this.testMapCreator);
    }

    @Override
    protected void init() {
        this.datFile = worldFolder.toPath().resolve(WorldMapConstants.MAP_DAT).toFile();
    }

    @Override
    public void read() {
        // no op
    }

    @Override
    public boolean allows(Player player) {
        return true;
    }
}
