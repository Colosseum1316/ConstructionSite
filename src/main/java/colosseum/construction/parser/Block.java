package colosseum.construction.parser;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

@SuppressWarnings({"FieldMayBeFinal", "deprecation"})
final class Block {
    @Getter
    @Setter
    private short id;
    @Setter
    @Getter
    private byte data;
    private Location location;

    public Block(short id, byte data, int x, int y, int z) {
        this.id = id;
        this.data = data;
        this.location = new Location(x, y, z);
    }

    public int getX() {
        return location.getBlockX();
    }

    public int getY() {
        return location.getBlockY();
    }

    public int getZ() {
        return location.getBlockZ();
    }

    public Location getLocation() {
        return new Location(location.getX(), location.getY(), location.getZ());
    }

    public boolean isMaterial(Material material) {
        return id == material.getId();
    }
}
