package colosseum.construction.test.dummies;

import be.seeseemelk.mockbukkit.WorldMock;
import colosseum.construction.ConstructionSiteProvider;
import colosseum.construction.WorldUtils;
import org.bukkit.Difficulty;

import java.io.File;

public final class ConstructionSiteWorldMock extends WorldMock {
    private Difficulty difficulty;
    private boolean isAutoSave;
    private boolean pvp;
    private long daytime;

    private final boolean map;

    public ConstructionSiteWorldMock(String name) {
        this(name, false);
    }

    public ConstructionSiteWorldMock(String name, boolean map) {
        super.setName(name);
        this.map = map;
    }

    /** getName is not used in actual plugin. MockBukkit relies on it. */
    @Override
    public String getName() {
        // Simulation.
        return map ? WorldUtils.getWorldRelativePath(getWorldFolder()) : super.getName();
    }

    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public boolean isAutoSave() {
        return isAutoSave;
    }

    @Override
    public void setAutoSave(boolean autoSave) {
        isAutoSave = autoSave;
    }

    @Override
    public boolean getPVP() {
        return pvp;
    }

    @Override
    public void setPVP(boolean pvp) {
        this.pvp = pvp;
    }

    @Override
    public long getTime() {
        long time = getFullTime() % 24000;
        if (time < 0) {
            time += 24000;
        }
        return time;
    }

    @Override
    public void setTime(long time) {
        long margin = (time - getFullTime()) % 24000;
        if (margin < 0) {
            margin += 24000;
        }
        setFullTime(getFullTime() + margin);
    }

    @Override
    public long getFullTime() {
        return daytime;
    }

    @Override
    public void setFullTime(long time) {
        this.daytime = time;
    }

    @Override
    public boolean setGameRuleValue(String rule, String value) {
        if (rule == null || value == null) {
            return false;
        }
        if (rule.equals("randomTickSpeed")) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return switch (rule) {
                case "doFireTick" -> true;
                case "mobGriefing" -> true;
                case "keepInventory" -> true;
                case "doMobSpawning" -> true;
                case "doMobLoot" -> true;
                case "doTileDrops" -> true;
                case "doEntityDrops" -> true;
                case "commandBlockOutput" -> true;
                case "naturalRegeneration" -> true;
                case "doDaylightCycle" -> true;
                case "logAdminCommands" -> true;
                case "showDeathMessages" -> true;
                case "sendCommandFeedback" -> true;
                case "reducedDebugInfo" -> true;
                default -> false;
            };
        }
    }

    @Override
    public File getWorldFolder() {
        // Simulation.
        return map ? WorldUtils.getSingleWorldRootPath(super.getName()) : new File(ConstructionSiteProvider.getSite().getWorldContainer(), super.getName());
    }
}
