package colosseum.construction.data;

import colosseum.utility.arcade.GameType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public interface MapData {
    boolean isCurrentlyLive();

    ImmutableMap<String, Vector> warps();

    ImmutableSet<UUID> adminList();

    GameType getMapGameType();

    String getMapName();

    String getMapCreator();

    boolean allows(Player player);
}
