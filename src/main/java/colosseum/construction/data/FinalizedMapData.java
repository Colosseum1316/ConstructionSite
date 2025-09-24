package colosseum.construction.data;

import colosseum.utility.MapData;
import colosseum.utility.arcade.GameType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public final class FinalizedMapData implements MapData {
    @Getter
    private final boolean live;
    private final ImmutableMap<String, Vector> warps;
    private final ImmutableSet<UUID> adminList;
    @Getter
    private final GameType mapGameType;
    @Getter
    private final String mapName;
    @Getter
    private final String mapCreator;

    public FinalizedMapData(MutableMapData mapData) {
        this.live = mapData.isLive();
        this.warps = mapData.warps();
        this.adminList = mapData.adminList();
        this.mapGameType = mapData.getMapGameType();
        this.mapName = mapData.getMapName();
        this.mapCreator = mapData.getMapCreator();
    }

    @Override
    public ImmutableMap<String, Vector> warps() {
        ImmutableMap.Builder<String, Vector> builder = ImmutableMap.builder();
        warps.forEach((s, l) -> builder.put(s, l.clone()));
        return builder.build();
    }

    @Override
    public ImmutableSet<UUID> adminList() {
        ImmutableSet.Builder<UUID> builder = ImmutableSet.builder();
        adminList.forEach(v -> builder.add(UUID.fromString(v.toString())));
        return builder.build();
    }

    @Override
    public boolean allows(Player player) {
        return player.isOp() || adminList.contains(player.getUniqueId());
    }
}
