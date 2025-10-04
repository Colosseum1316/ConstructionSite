package colosseum.construction.data;

import colosseum.utility.MapData;
import colosseum.utility.arcade.GameType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@AllArgsConstructor
public final class FinalizedMapData implements MapData {
    @Getter @Nullable private final String mapName;
    @Getter @Nullable private final String mapCreator;
    @Getter @Nullable private final GameType mapGameType;
    @Nullable private final ImmutableMap<String, Vector> warps;
    @Nullable private final ImmutableSet<UUID> adminList;
    @Getter private final boolean live;

    public FinalizedMapData(@NonNull MapData mapData) {
        this(mapData.getMapName(), mapData.getMapCreator(), mapData.getMapGameType(), mapData.warps(), mapData.adminList(), mapData.isLive());
    }

    public FinalizedMapData(
            @Nullable String mapName,
            @Nullable String mapCreator,
            @Nullable GameType mapGameType,
            @Nullable ImmutableMap<String, Vector> warps,
            boolean live
    ) {
        this(mapName, mapCreator, mapGameType, warps, null, live);
    }

    public FinalizedMapData(
            @Nullable String mapName,
            @Nullable String mapCreator,
            @Nullable GameType mapGameType,
            @Nullable ImmutableSet<UUID> adminList,
            boolean live
    ) {
        this(mapName, mapCreator, mapGameType, null, adminList, live);
    }

    public FinalizedMapData(
            @Nullable String mapName,
            @Nullable String mapCreator,
            @Nullable GameType mapGameType,
            boolean live
    ) {
        this(mapName, mapCreator, mapGameType, null, null, live);
    }

    @Nullable
    @Override
    public ImmutableMap<String, Vector> warps() {
        return warps;
    }

    @Nullable
    @Override
    public ImmutableSet<UUID> adminList() {
        return adminList;
    }

    @Override
    public boolean allows(Player player) {
        return player.isOp() || (adminList != null && adminList.contains(player.getUniqueId()));
    }
}
