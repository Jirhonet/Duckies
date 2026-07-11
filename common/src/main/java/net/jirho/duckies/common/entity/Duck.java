package net.jirho.duckies.common.entity;

import java.util.UUID;

import net.jirho.duckies.common.advancement.DuckiesAdvancements;
import net.jirho.duckies.init.DuckiesRegistries;
import net.jirho.duckies.init.DuckiesTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Duck extends TamableAnimal implements NeutralMob {
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(Duck.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Duck.class,
            EntityDataSerializers.INT);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final int HELD_ITEM_CONSUME_DELAY = 100;
    private static final float WATER_SWIM_SPEED_SCALE = 0.5F;
    private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Duck.class,
            EntityDataSerializers.BOOLEAN);
    private UUID persistentAngerTarget;
    private int ticksSinceEaten;
    private UUID consumableProvider;

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
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(6, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(7,
                new DuckTemptGoal(this, 1.0D, Ingredient.of(DuckiesTags.DUCK_EDIBLE), false));
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
        tag.putByte("Color", (byte) this.getColor().getId());
        this.addPersistentAngerSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Color", 99)) {
            this.setColor(DyeColor.byId(tag.getInt("Color")));
        }
        this.readPersistentAngerSaveData(this.level, tag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_COLOR, DyeColor.RED.getId());
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    public DyeColor getColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLOR));
    }

    public void setColor(DyeColor color) {
        this.entityData.set(DATA_COLOR, color.getId());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            this.updatePersistentAnger((ServerLevel) this.level, true);
            ++this.ticksSinceEaten;
            this.tryConsumeHeldItem();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.onGround) {
            Vec3 movement = this.getDeltaMovement();
            if (movement.y < 0.0D) {
                this.setDeltaMovement(movement.multiply(1.0D, 0.6D, 1.0D));
            }
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
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
        return this.canHoldItem(stack);
    }

    public boolean canHoldItem(ItemStack stack) {
        if (this.isDuckweed(stack)) {
            return !this.isHoldingDuckweed();
        }
        ItemStack heldItem = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (heldItem.isEmpty()) {
            return true;
        }
        return this.getPickupPriority(stack) > this.getPickupPriority(heldItem);
    }

    public int getPickupPriority(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (this.isDuckweed(stack)) {
            return 3;
        }
        if (this.isDuckEdible(stack) || this.isDrinkablePotion(stack)) {
            return 2;
        }
        return 1;
    }

    public boolean isDuckEdible(ItemStack stack) {
        return !this.isDuckweed(stack) && stack.is(DuckiesTags.DUCK_EDIBLE);
    }

    public boolean isDrinkablePotion(ItemStack stack) {
        return stack.is(Items.POTION) && this.hasEffectivePotionEffects(stack);
    }

    private boolean hasEffectivePotionEffects(ItemStack stack) {
        return !PotionUtils.getMobEffects(stack).isEmpty();
    }

    public boolean isHoldingConsumable() {
        ItemStack heldItem = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return this.isConsumable(heldItem);
    }

    private boolean isConsumable(ItemStack stack) {
        return this.isDuckweed(stack) || this.isDuckEdible(stack) || this.isDrinkablePotion(stack);
    }

    private boolean isDuckweed(ItemStack stack) {
        return stack.is(DuckiesRegistries.DUCKWEED_ITEM.get());
    }

    private boolean isHoldingDuckweed() {
        return this.isDuckweed(this.getItemBySlot(EquipmentSlot.MAINHAND));
    }

    private void tryConsumeHeldItem() {
        ItemStack heldItem = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (heldItem.isEmpty() || this.ticksSinceEaten < HELD_ITEM_CONSUME_DELAY) {
            return;
        }
        if (this.isDuckweed(heldItem) || this.isDuckEdible(heldItem)) {
            this.consumeHeldFood(heldItem);
        } else if (this.isDrinkablePotion(heldItem)) {
            this.drinkHeldPotion(heldItem);
        }
    }

    private void consumeHeldFood(ItemStack heldItem) {
        ItemStack eatenItem = heldItem.copy();
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
        this.spawnEatParticles(eatenItem);
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0F);
        }
        this.ticksSinceEaten = 0;
    }

    private void drinkHeldPotion(ItemStack heldItem) {
        ItemStack drunkItem = heldItem.copy();
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GLASS_BOTTLE));
        this.playSound(SoundEvents.GENERIC_DRINK, 1.0F, 1.0F);
        for (MobEffectInstance effect : PotionUtils.getMobEffects(drunkItem)) {
            if (effect.getEffect().isInstantenous()) {
                effect.getEffect().applyInstantenousEffect(this, this, this, effect.getAmplifier(), 1.0D);
            } else {
                this.addEffect(new MobEffectInstance(effect));
            }
        }
        if (this.consumableProvider != null && this.level instanceof ServerLevel serverLevel) {
            Player player = serverLevel.getPlayerByUUID(this.consumableProvider);
            if (player instanceof ServerPlayer serverPlayer) {
                DuckiesAdvancements.grant(serverPlayer, DuckiesAdvancements.LEMONADE_STAND, "fed_potion");
            }
        }
        this.consumableProvider = null;
        this.ticksSinceEaten = 0;
    }

    private void spawnEatParticles(ItemStack stack) {
        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, stack),
                    this.getX(),
                    this.getY() + (double) this.getBbHeight() * 0.5D,
                    this.getZ(),
                    7,
                    0.2D,
                    0.1D,
                    0.2D,
                    0.05D);
        }
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (!this.canHoldItem(stack)) {
            return;
        }

        ItemStack pickedUpItem = stack.copy();
        pickedUpItem.setCount(1);

        int count = stack.getCount();
        if (count > 1) {
            this.dropItemStack(stack.split(count - 1));
        }

        this.spitOutItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
        this.onItemPickup(itemEntity);
        this.setItemSlot(EquipmentSlot.MAINHAND, stack.split(1));
        this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        this.take(itemEntity, pickedUpItem.getCount());
        itemEntity.discard();
        this.ticksSinceEaten = 0;

        UUID provider = this.getItemProvider(itemEntity);
        if (provider != null && this.level instanceof ServerLevel serverLevel) {
            Player player = serverLevel.getPlayerByUUID(provider);
            if (player instanceof ServerPlayer serverPlayer) {
                if (pickedUpItem.getItem() instanceof SwordItem) {
                    DuckiesAdvancements.grant(serverPlayer, DuckiesAdvancements.PEACE_WAS_NEVER_AN_OPTION,
                            "gave_sword");
                } else if (pickedUpItem.is(Items.POTION) && this.hasEffectivePotionEffects(pickedUpItem)) {
                    this.consumableProvider = provider;
                }
            }
        }
    }

    private UUID getItemProvider(ItemEntity itemEntity) {
        UUID thrower = itemEntity.getThrower();
        if (thrower != null) {
            return thrower;
        }
        return itemEntity.getOwner();
    }

    private void spitOutItem(ItemStack stack) {
        if (stack.isEmpty() || this.level.isClientSide) {
            return;
        }

        if (stack.is(Items.POTION)) {
            this.consumableProvider = null;
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

    private void feedDuckEdible(ItemStack food) {
        this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
        this.spawnEatParticles(food);
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (this.level.isClientSide) {
            if ((this.isFood(itemStack) || this.isDuckEdible(itemStack)) && this.getHealth() < this.getMaxHealth()) {
                return InteractionResult.CONSUME;
            }
            if (this.isTame() && itemStack.getItem() instanceof DyeItem) {
                return InteractionResult.CONSUME;
            }
            if (this.isTame() && this.isOwnedBy(player) && !this.isFood(itemStack)) {
                return InteractionResult.CONSUME;
            }
            if (!this.isTame() && this.isFood(itemStack)) {
                return InteractionResult.CONSUME;
            }
            return super.mobInteract(player, hand);
        }

        if (this.isTame()) {
            if ((this.isFood(itemStack) || this.isDuckEdible(itemStack)) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                this.feedDuckEdible(itemStack);
                return InteractionResult.CONSUME;
            }

            Item item = itemStack.getItem();
            if (item instanceof DyeItem dyeItem) {
                DyeColor dyeColor = dyeItem.getDyeColor();
                if (dyeColor != this.getColor()) {
                    this.setColor(dyeColor);
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
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

        if ((this.isFood(itemStack) || this.isDuckEdible(itemStack)) && this.getHealth() < this.getMaxHealth()) {
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            this.feedDuckEdible(itemStack);
            return InteractionResult.CONSUME;
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
        Duck duck = DuckiesRegistries.DUCK.get().create(level);
        UUID owner = this.getOwnerUUID();
        if (owner != null) {
            duck.setOwnerUUID(owner);
            duck.setTame(true);
        }
        duck.setColor(this.getOffspringColor((Duck) parent));
        return duck;
    }

    private DyeColor getOffspringColor(Duck otherParent) {
        DyeColor parentColor = this.getColor();
        DyeColor otherColor = otherParent.getColor();
        CraftingContainer container = makeDyeContainer(parentColor, otherColor);
        return this.level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, container, this.level)
                .map(recipe -> recipe.assemble(container))
                .map(ItemStack::getItem)
                .filter(DyeItem.class::isInstance)
                .map(DyeItem.class::cast)
                .map(DyeItem::getDyeColor)
                .orElseGet(() -> this.random.nextBoolean() ? parentColor : otherColor);
    }

    private static CraftingContainer makeDyeContainer(DyeColor first, DyeColor second) {
        CraftingContainer container = new CraftingContainer(new AbstractContainerMenu(null, -1) {
            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return false;
            }
        }, 2, 1);
        container.setItem(0, new ItemStack(DyeItem.byColor(first)));
        container.setItem(1, new ItemStack(DyeItem.byColor(second)));
        return container;
    }

    @Override
    public void travel(Vec3 vector) {
        if (this.isEffectiveAi() && this.isInWater() && this.isAffectedByFluids()) {
            double yBefore = this.getY();
            float waterSlowDown = this.getWaterSlowDown();

            this.moveRelative(this.getSpeed() * WATER_SWIM_SPEED_SCALE, vector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 movement = this.getDeltaMovement();

            if (this.horizontalCollision && this.onClimbable()) {
                movement = new Vec3(movement.x, 0.2D, movement.z);
            }

            this.setDeltaMovement(movement.multiply(waterSlowDown, 0.8D, waterSlowDown));
            movement = this.getFluidFallingAdjustedMovement(0.08D, movement.y <= 0.0D, this.getDeltaMovement());
            this.setDeltaMovement(movement);

            if (this.horizontalCollision) {
                movement = this.getDeltaMovement();
                if (this.isFree(movement.x, movement.y + 0.6D - this.getY() + yBefore, movement.z)) {
                    this.setDeltaMovement(movement.x, 0.3D, movement.z);
                }
            }
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
