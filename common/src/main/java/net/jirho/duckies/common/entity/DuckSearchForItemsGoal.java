package net.jirho.duckies.common.entity;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

public class DuckSearchForItemsGoal extends Goal {
    private static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive();
    private final Duck duck;

    public DuckSearchForItemsGoal(Duck duck) {
        this.duck = duck;
        this.setFlags(java.util.EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.duck.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            return false;
        }
        if (this.duck.getTarget() != null || this.duck.getLastHurtByMob() != null) {
            return false;
        }
        if (!this.duck.canMove()) {
            return false;
        }
        if (this.duck.getRandom().nextInt(reducedTickDelay(10)) != 0) {
            return false;
        }
        return !this.findNearbyItems().isEmpty();
    }

    @Override
    public void start() {
        List<ItemEntity> items = this.findNearbyItems();
        if (!items.isEmpty()) {
            this.duck.getNavigation().moveTo(items.get(0), 1.2D);
        }
    }

    @Override
    public void tick() {
        if (this.duck.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            List<ItemEntity> items = this.findNearbyItems();
            if (!items.isEmpty()) {
                this.duck.getNavigation().moveTo(items.get(0), 1.2D);
            }
        }
    }

    private List<ItemEntity> findNearbyItems() {
        AABB searchArea = this.duck.getBoundingBox().inflate(8.0D, 8.0D, 8.0D);
        return this.duck.level.getEntitiesOfClass(ItemEntity.class, searchArea, ALLOWED_ITEMS);
    }
}
