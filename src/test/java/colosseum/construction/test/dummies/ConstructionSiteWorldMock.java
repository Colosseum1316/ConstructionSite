package colosseum.construction.test.dummies;

import be.seeseemelk.mockbukkit.WorldMock;
import colosseum.construction.ConstructionSiteProvider;
import org.bukkit.Difficulty;

import java.io.File;

public final class ConstructionSiteWorldMock extends WorldMock {
    private Difficulty difficulty;
    private boolean isAutoSave;
    private boolean pvp;

    public ConstructionSiteWorldMock(String name) {
        super();
        super.setName(name);
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
    public void setTime(long time) {
        // no op
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
        return new File(ConstructionSiteProvider.getSite().getWorldContainer(), getName());
    }
}
