package colosseum.construction.data;

import colosseum.utility.MapData;
import colosseum.utility.arcade.GameType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface MutableMapData extends MapData {
    void setMapGameType(GameType mapGameType);

    void setMapName(String mapName);

    void setMapCreator(String mapCreator);

    void setLive(boolean live);

    Map<String, Vector> getWarps();

    Set<UUID> getAdminList();

    void write();
}
