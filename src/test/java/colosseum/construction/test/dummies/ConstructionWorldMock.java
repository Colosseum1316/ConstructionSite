package colosseum.construction.test.dummies;

import be.seeseemelk.mockbukkit.WorldMock;
import colosseum.construction.ConstructionSiteProvider;

import java.io.File;

public class ConstructionWorldMock extends WorldMock {
    @Override
    public File getWorldFolder() {
        return new File(ConstructionSiteProvider.getSite().getWorldContainer(), getName());
    }
}
