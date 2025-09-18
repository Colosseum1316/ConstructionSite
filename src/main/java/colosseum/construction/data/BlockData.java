package colosseum.construction.data;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;

@Getter
@SuppressWarnings({"unused", "deprecation"})
public class BlockData {
    private final Block block;
    private final Material material;
    private final byte data;

    public BlockData(Block block) {
        this.block = block;
        this.material = block.getType();
        this.data = block.getData();
    }

    public void restore() {
        block.setType(material);
        block.setData(data);
    }

    public void restore(boolean applyPhysics) {
        block.setType(material);
        block.setData(data, applyPhysics);
    }
}
