package colosseum.construction.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@Getter
public final class FinalizedMapData {
    private final Optional<String> mapName;
    private final Optional<String> mapCreator;
    private final Optional<ImmutableMap<String, Vector>> warps;
    private final Optional<ImmutableSet<UUID>> adminList;
    private final Optional<Boolean> live;

    public FinalizedMapData(@NonNull MapData mapData) {
        this(mapData.getMapName(), mapData.getMapCreator(), mapData.warps(), mapData.adminList(), mapData.isLive());
    }

    public FinalizedMapData(String mapName, String mapCreator, ImmutableMap<String, Vector> warps, ImmutableSet<UUID> adminList, Boolean live) {
        this.mapName = Optional.ofNullable(mapName);
        this.mapCreator = Optional.ofNullable(mapCreator);
        this.warps = Optional.ofNullable(warps);
        this.adminList = Optional.ofNullable(adminList);
        this.live = Optional.ofNullable(live);
    }

    public FinalizedMapData(String mapName, String mapCreator) {
        this(mapName, mapCreator, null, null, null);
    }

    public FinalizedMapData(
            @Nullable String mapName,
            @Nullable String mapCreator,
            @Nullable ImmutableMap<String, Vector> warps
    ) {
        this(mapName, mapCreator, warps, null, null);
    }

    public FinalizedMapData(
            @Nullable ImmutableMap<String, Vector> warps
    ) {
        this(null, null, warps, null, null);
    }

    public FinalizedMapData(
            @Nullable String mapName,
            @Nullable String mapCreator,
            @Nullable ImmutableSet<UUID> adminList
    ) {
        this(mapName, mapCreator, null, adminList, null);
    }

    public FinalizedMapData(
            @Nullable ImmutableSet<UUID> adminList
    ) {
        this(null, null, null, adminList, null);
    }

    public FinalizedMapData(
            @Nullable Boolean live
    ) {
        this(null, null, null, null, live);
    }
}
