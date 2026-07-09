package net.jirho.duckies.common.entity;

import java.util.EnumSet;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class DuckSwimGoal extends Goal {
    private final PathfinderMob duck;

    public DuckSwimGoal(PathfinderMob duck) {
        this.duck = duck;
        this.setFlags(EnumSet.of(Flag.JUMP));
        duck.getNavigation().setCanFloat(true);
    }

    @Override
    public boolean canUse() {
        return this.duck.isInWater() && this.duck.getFluidHeight(FluidTags.WATER) > (this.duck.isBaby() ? 0.1D : 0.2D) || this.duck.isInLava();
    }

    @Override
    public void tick() {
        if (this.duck.getRandom().nextFloat() < 0.8F) {
            this.duck.getJumpControl().jump();
        }
    }
}
