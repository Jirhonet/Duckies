package net.jirho.duckies.common.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;

public class DuckHurtByTargetGoal extends HurtByTargetGoal {
    public DuckHurtByTargetGoal(Duck duck) {
        super(duck);
        this.setAlertOthers();
    }

    @Override
    public void start() {
        super.start();
        this.angerDuck(this.mob);
    }

    @Override
    protected void alertOther(Mob mob, LivingEntity target) {
        super.alertOther(mob, target);
        this.angerDuck(mob);
    }

    private void angerDuck(Mob mob) {
        if (mob instanceof Duck duck && !duck.isTame()) {
            LivingEntity attacker = duck.getLastHurtByMob();
            duck.startPersistentAngerTimer();
            if (attacker != null) {
                duck.setPersistentAngerTarget(attacker.getUUID());
            }
        }
    }
}
