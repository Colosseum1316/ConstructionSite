package colosseum.construction.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public interface MapData {
    boolean isLive();

    ImmutableMap<String, Vector> warps();

    ImmutableSet<UUID> adminList();

    String getMapName();

    String getMapCreator();

    boolean allows(Player player);
}
