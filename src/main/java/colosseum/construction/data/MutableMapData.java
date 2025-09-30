package colosseum.construction.data;

import colosseum.utility.MapData;

public interface MutableMapData extends MapData {
    boolean updateAndWrite(FinalizedMapData newMapData);

    default boolean save() {
        return true;
    }
}
