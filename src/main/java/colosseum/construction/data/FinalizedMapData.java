package colosseum.construction.data;

import colosseum.utility.MapData;
import colosseum.utility.arcade.GameType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

@AllArgsConstructor
public final class FinalizedMapData implements MapData {
    @Getter
    private final String mapName;
    @Getter
    private final String mapCreator;
    @Getter
    private final GameType mapGameType;
    private final ImmutableMap<String, Vector> warps;
    private final ImmutableSet<UUID> adminList;
    @Getter
    private final boolean live;

    public FinalizedMapData(MapData mapData) {
        this(mapData.getMapName(), mapData.getMapCreator(), mapData.getMapGameType(), mapData.warps(), mapData.adminList(), mapData.isLive());
    }

    @Override
    public ImmutableMap<String, Vector> warps() {
        return warps;
    }

    @Override
    public ImmutableSet<UUID> adminList() {
        return adminList;
    }

    @Override
    public boolean allows(Player player) {
        return player.isOp() || adminList.contains(player.getUniqueId());
    }
}
