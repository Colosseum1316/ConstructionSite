package colosseum.construction.parser;

import lombok.Getter;
import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.anvil.AnvilChunk;
import nl.rutgerkok.hammer.anvil.AnvilMaterialMap;
import nl.rutgerkok.hammer.anvil.AnvilWorld;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;

import java.io.File;
import java.util.HashMap;

@SuppressWarnings({"unchecked", "deprecation"})
final class World {
    private final File directory;
    @Getter
    private nl.rutgerkok.hammer.World offlineWorld;
    @Getter
    private final HashMap<Pair<Integer, Integer>, AnvilChunk> visitedChunks = new HashMap<>();

    public World(File directory) throws Exception {
        this.directory = directory;
        readOfflineWorld();
    }

    private void readOfflineWorld() throws Exception {
        this.offlineWorld = new AnvilWorld(new GlobalMaterialMap(), directory.toPath().resolve(AnvilWorld.LEVEL_DAT_NAME));
    }

    public ChunkAccess<AnvilChunk> getChunkAccess() throws Exception {
        return (ChunkAccess<AnvilChunk>) offlineWorld.getChunkAccess();
    }

    public AnvilChunk getChunk(ChunkAccess<AnvilChunk> chunkAccess, int blockX, int blockZ) {
        int chunkX = Math.floorDiv(blockX, AnvilChunk.CHUNK_X_SIZE);
        int chunkZ = Math.floorDiv(blockZ, AnvilChunk.CHUNK_Z_SIZE);
        return visitedChunks.computeIfAbsent(Pair.of(chunkX, chunkZ), p -> {
            try {
                return chunkAccess.getChunk(p.getLeft(), p.getRight());
            } catch (Exception e) {
                throw new Error(e);
            }
        });
    }

    public Block getBlock(AnvilChunk chunk, int blockX, int blockY, int blockZ) {
        char id = ((AnvilMaterialMap) offlineWorld.getGameFactory().getMaterialMap()).getOldMinecraftId(chunk.getMaterial(Math.floorMod(blockX, AnvilChunk.CHUNK_X_SIZE), blockY, Math.floorMod(blockZ, AnvilChunk.CHUNK_Z_SIZE)));
        short typeId = (short) (id >> 4);
        byte data = (byte) (id & 0xF);
        return new Block(typeId, data, blockX, blockY, blockZ);
    }

    public String[] readSign(AnvilChunk chunk, Block signBlock) {
        CompoundTag signTag = chunk.getTileEntities().stream().filter(v ->
                v.getString(AnvilFormat.TileEntityTag.ID).equals("Sign")
                && v.getInt(AnvilFormat.TileEntityTag.X_POS) == signBlock.getX()
                && v.getInt(AnvilFormat.TileEntityTag.Y_POS) == signBlock.getY()
                && v.getInt(AnvilFormat.TileEntityTag.Z_POS) == signBlock.getZ()).findFirst().orElse(null);
        if (signTag == null) {
            throw new IllegalStateException("There's no sign.");
        }
        String[] res = new String[]{"", "", "", ""};
        for (int i = 0; i < 4; i++) {
            res[i] = signTag.getString(AnvilFormat.TileEntityTag.SIGN_LINE_NAMES.get(i));
        }
        return res;
    }

    public void setAir(Block block) throws Exception {
        setBlock(block, (short) Material.AIR.getId(), (byte) 0);
    }

    public void setBlock(Block block, short newTypeId, byte newData) throws Exception {
        block.setId(newTypeId);
        block.setData(newData);
        setBlock(block);
    }

    public void setBlock(Block block) throws Exception {
        setBlock(block.getX(), block.getY(), block.getZ(), block.getId(), block.getData());
    }

    private void setBlock(int blockX, int blockY, int blockZ, short typeId, byte data) throws Exception {
        try (ChunkAccess<AnvilChunk> chunkAccess = getChunkAccess()) {
            AnvilChunk chunk = getChunk(chunkAccess, blockX, blockZ);
            chunk.setMaterial(Math.floorMod(blockX, AnvilChunk.CHUNK_X_SIZE), blockY, Math.floorMod(blockZ, AnvilChunk.CHUNK_Z_SIZE), ((AnvilMaterialMap) offlineWorld.getGameFactory().getMaterialMap()).getMaterialDataFromOldIds(typeId, data));
        }
    }
}
