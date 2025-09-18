package colosseum.construction.test.dummies;

import be.seeseemelk.mockbukkit.entity.PlayerMock;

import java.util.UUID;

public class ConstructionPlayerMock extends PlayerMock {

    private boolean flying = false;
    private float flySpeed = 1.0f;

    public ConstructionPlayerMock(String name) {
        super(name);
    }

    public ConstructionPlayerMock(String name, UUID uuid) {
        super(name, uuid);
    }

    @Override
    public boolean getAllowFlight() {
        return true;
    }

    @Override
    public boolean isFlying() {
        return flying;
    }

    @Override
    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    @Override
    public float getFlySpeed() {
        return flySpeed;
    }

    @Override
    public void setFlySpeed(float flySpeed) {
        if (flySpeed < -1.0 || flySpeed > 1.0) {
            throw new IllegalArgumentException("Invalid fly speed: " + flySpeed);
        }
        this.flySpeed = flySpeed;
    }
}
