package net.jirho.duckies.common.entity;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.item.crafting.Ingredient;

public class DuckTemptGoal extends TemptGoal {
    private final Duck duck;

    public DuckTemptGoal(Duck duck, double speed, Ingredient items, boolean canScare) {
        super(duck, speed, items, canScare);
        this.duck = duck;
    }

    @Override
    public boolean canUse() {
        if (this.duck.isHoldingConsumable()) {
            return false;
        }
        return super.canUse();
    }
}
