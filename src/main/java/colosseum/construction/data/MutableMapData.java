package colosseum.construction.data;

public interface MutableMapData extends MapData {
    void update(FinalizedMapData newMapData);

    boolean write();
}
