package net.jirho.duckies.common.entity;

import java.util.function.Predicate;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.ItemStack;

public class DuckTemptGoal extends TemptGoal {
    private final Duck duck;

    public DuckTemptGoal(Duck duck, double speed, Predicate<ItemStack> items, boolean canScare) {
        super(duck, speed, items, canScare);
        this.duck = duck;
    }

    @Override
    public boolean canUse() {
        if (this.duck.isHoldingConsumable() || this.duck.isAngry() || this.duck.getTarget() != null) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.duck.isAngry() || this.duck.getTarget() != null) {
            return false;
        }
        return super.canContinueToUse();
    }
}
