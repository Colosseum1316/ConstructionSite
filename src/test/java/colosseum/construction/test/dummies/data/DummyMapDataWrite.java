package colosseum.construction.test.dummies.data;

import colosseum.construction.WorldUtils;
import colosseum.construction.data.FinalizedMapData;
import colosseum.construction.data.MapDataImpl;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DummyMapDataWrite extends MapDataImpl {
    private final String testMapName;
    private final String testMapCreator;
    private final ImmutableMap<String, Vector> testWarps;
    private final ImmutableSet<UUID> testAdminList;
    private final boolean testCurrentlyLive;

    public DummyMapDataWrite(
            World world,
            File worldFolder,
            @NonNull String mapName,
            @NonNull String mapCreator,
            @NonNull Map<String, Vector> warps,
            @NonNull Set<UUID> adminList,
            boolean currentlyLive
    ) {
        super(world, worldFolder);

        this.testMapName = mapName;
        this.testMapCreator = mapCreator;

        ImmutableMap.Builder<String, Vector> mapBuilder = ImmutableMap.builder();
        warps.forEach((s, l) -> mapBuilder.put(s, l.clone()));
        this.testWarps = mapBuilder.build();

        ImmutableSet.Builder<UUID> setBuilder = ImmutableSet.builder();
        adminList.forEach(v -> setBuilder.add(UUID.fromString(v.toString())));
        this.testAdminList = setBuilder.build();

        this.testCurrentlyLive = currentlyLive;

        super.update(new FinalizedMapData(this.testMapName, this.testMapCreator, this.testWarps, this.testAdminList, this.testCurrentlyLive));
    }

    @Override
    protected void init() {
        this.datFile = WorldUtils.mapDatFile(worldFolder);
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
