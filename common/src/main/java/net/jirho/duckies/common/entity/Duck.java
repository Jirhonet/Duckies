package net.jirho.duckies.common.entity;

import java.util.UUID;

import net.jirho.duckies.init.DuckiesRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Duck extends TamableAnimal implements NeutralMob {
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Duck.class,
            EntityDataSerializers.INT);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final int DUCKWEED_EAT_DELAY = 100;
    private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Duck.class,
            EntityDataSerializers.BOOLEAN);
    private UUID persistentAngerTarget;
    private int ticksSinceEaten;

    public Duck(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new DuckSwimGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5,
                new TemptGoal(this, 1.0D, Ingredient.of(DuckiesRegistries.DUCKWEED_ITEM.get()), false));
        this.goalSelector.addGoal(6, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(7, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(8, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(12, new DuckSearchForItemsGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new DuckHurtByTargetGoal(this));
        this.targetSelector.addGoal(4,
                new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.addPersistentAngerSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readPersistentAngerSaveData(this.level, tag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            this.updatePersistentAnger((ServerLevel) this.level, true);
            ++this.ticksSinceEaten;
            this.tryEatHeldDuckweed();
        }
    }

    public boolean canMove() {
        return !this.isOrderedToSit();
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        EquipmentSlot slot = Mob.getEquipmentSlotForItem(stack);
        if (slot != EquipmentSlot.MAINHAND || !super.canTakeItem(stack)) {
            return false;
        }
        if (this.isDuckweed(stack)) {
            return true;
        }
        return this.getItemBySlot(slot).isEmpty();
    }

    public boolean canHoldItem(ItemStack stack) {
        if (this.isDuckweed(stack)) {
            return true;
        }
        ItemStack heldItem = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (heldItem.isEmpty()) {
            return true;
        }
        return this.ticksSinceEaten > 0 && stack.getItem().isEdible() && !heldItem.getItem().isEdible();
    }

    private boolean isDuckweed(ItemStack stack) {
        return stack.is(DuckiesRegistries.DUCKWEED_ITEM.get());
    }

    private void tryEatHeldDuckweed() {
        ItemStack heldItem = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!this.isDuckweed(heldItem) || this.ticksSinceEaten < DUCKWEED_EAT_DELAY) {
            return;
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0F);
        }
        this.ticksSinceEaten = 0;
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (!this.canHoldItem(stack)) {
            return;
        }

        int count = stack.getCount();
        if (count > 1) {
            this.dropItemStack(stack.split(count - 1));
        }

        this.spitOutItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
        this.onItemPickup(itemEntity);
        this.setItemSlot(EquipmentSlot.MAINHAND, stack.split(1));
        this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        this.take(itemEntity, stack.getCount());
        itemEntity.discard();
        this.ticksSinceEaten = 0;
    }

    private void spitOutItem(ItemStack stack) {
        if (stack.isEmpty() || this.level.isClientSide) {
            return;
        }

        ItemEntity itemEntity = new ItemEntity(this.level, this.getX() + this.getLookAngle().x, this.getY() + 1.0D,
                this.getZ() + this.getLookAngle().z, stack);
        itemEntity.setPickUpDelay(40);
        itemEntity.setThrower(this.getUUID());
        this.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
        this.level.addFreshEntity(itemEntity);
    }

    private void dropItemStack(ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), stack);
        this.level.addFreshEntity(itemEntity);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    public void setSuffocating(boolean suffocating) {
        this.entityData.set(DATA_SUFFOCATING, suffocating);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(DuckiesRegistries.DUCKWEED_ITEM.get());
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        return otherAnimal instanceof Duck other
                && this.isTame()
                && other.isTame()
                && super.canMate(otherAnimal);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (this.level.isClientSide) {
            if (this.isTame() && this.isOwnedBy(player) && !this.isFood(itemStack)) {
                return InteractionResult.CONSUME;
            }
            if (!this.isTame() && this.isFood(itemStack)) {
                return InteractionResult.CONSUME;
            }
            return super.mobInteract(player, hand);
        }

        if (this.isTame()) {
            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                this.heal(2.0F);
                return InteractionResult.CONSUME;
            }

            InteractionResult interactionResult = super.mobInteract(player, hand);
            if (!interactionResult.consumesAction() && this.isOwnedBy(player)) {
                this.setOrderedToSit(!this.isOrderedToSit());
                return InteractionResult.SUCCESS;
            }
            return interactionResult;
        }

        if (this.isFood(itemStack)) {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.level.broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level.broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level.isClientSide) {
            this.setOrderedToSit(false);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isAngryAt(LivingEntity entity) {
        return !this.isTame() && NeutralMob.super.isAngryAt(entity);
    }

    @Override
    public boolean isAggressive() {
        return this.isAngry() || this.getTarget() != null;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return this.isBaby() ? dimensions.height * 0.85F : dimensions.height * 0.92F;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob parent) {
        return DuckiesRegistries.DUCK.get().create(level);
    }

    @Override
    public void travel(Vec3 vector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), vector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2D));
        } else {
            super.travel(vector);
        }
    }

    @Override
    public ResourceLocation getDefaultLootTable() {
        return new ResourceLocation("duckies", "entities/duck");
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, time);
    }

    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(UUID id) {
        this.persistentAngerTarget = id;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAggressive() ? DuckiesRegistries.DUCK_ATTACK.get() : DuckiesRegistries.DUCK_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return DuckiesRegistries.DUCK_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DuckiesRegistries.DUCK_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    public static boolean canSpawn(EntityType<Duck> entity, LevelAccessor level, MobSpawnType spawnType, BlockPos pos,
            RandomSource random) {
        return Animal.checkAnimalSpawnRules(entity, level, spawnType, pos, random);
    }
}
