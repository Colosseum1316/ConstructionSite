package colosseum.construction.data;

import colosseum.utility.arcade.GameType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

public final class DummyMapData extends AbstractMapData {

    public DummyMapData() {
        super(null, null);
    }

    @Override
    protected void init() {
        // no op
    }

    @Override
    public void read() {
        // no op
    }

    @Override
    public boolean isLive() {
        return true;
    }

    @Override
    public ImmutableMap<String, Vector> warps() {
        return ImmutableMap.of();
    }

    @Override
    public ImmutableSet<UUID> adminList() {
        return ImmutableSet.of();
    }

    @Override
    public GameType getMapGameType() {
        return GameType.None;
    }

    @Override
    public String getMapName() {
        return "Lobby";
    }

    @Override
    public String getMapCreator() {
        return "Colosseum";
    }

    @Override
    public boolean allows(Player player) {
        return true;
    }
}
