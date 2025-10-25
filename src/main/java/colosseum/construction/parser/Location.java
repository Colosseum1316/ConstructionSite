package colosseum.construction.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.Vector;

import static org.bukkit.Location.locToBlock;

@AllArgsConstructor
@Getter
@SuppressWarnings("ClassCanBeRecord")
final class Location {
    private final double x;
    private final double y;
    private final double z;

    public int getBlockX() {
        return locToBlock(this.x);
    }

    public int getBlockY() {
        return locToBlock(this.y);
    }

    public int getBlockZ() {
        return locToBlock(this.z);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }
}
