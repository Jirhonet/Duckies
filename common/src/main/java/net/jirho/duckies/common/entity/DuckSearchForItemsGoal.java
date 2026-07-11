package net.jirho.duckies.common.entity;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
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
        ItemStack heldItem = this.duck.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!heldItem.isEmpty() && this.duck.getPickupPriority(heldItem) >= 3) {
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
        List<ItemEntity> items = this.findNearbyItems();
        if (!items.isEmpty()) {
            this.duck.getNavigation().moveTo(items.get(0), 1.2D);
        }
    }

    private List<ItemEntity> findNearbyItems() {
        AABB searchArea = this.duck.getBoundingBox().inflate(8.0D, 8.0D, 8.0D);
        return this.duck.level.getEntitiesOfClass(ItemEntity.class, searchArea, ALLOWED_ITEMS).stream()
                .filter(itemEntity -> this.duck.canHoldItem(itemEntity.getItem()))
                .sorted(Comparator.comparingInt((ItemEntity itemEntity) -> this.duck.getPickupPriority(itemEntity.getItem()))
                        .reversed()
                        .thenComparingDouble(itemEntity -> itemEntity.distanceToSqr(this.duck)))
                .toList();
    }
}
