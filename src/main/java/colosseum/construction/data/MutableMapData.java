package colosseum.construction.data;

import colosseum.utility.MapData;

public interface MutableMapData extends MapData {
    void update(FinalizedMapData newMapData);

    boolean write();
}
