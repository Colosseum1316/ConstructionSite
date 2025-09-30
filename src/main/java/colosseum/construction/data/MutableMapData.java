package colosseum.construction.data;

import colosseum.utility.MapData;

public interface MutableMapData extends MapData {
    default boolean updateAndWrite(FinalizedMapData newMapData) {
        return true;
    }

    default boolean save() {
        return true;
    }
}
