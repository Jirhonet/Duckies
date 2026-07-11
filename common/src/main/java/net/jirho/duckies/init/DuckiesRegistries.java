package net.jirho.duckies.init;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.jirho.duckies.Duckies;
import net.jirho.duckies.common.block.DuckweedBlock;
import net.jirho.duckies.common.entity.Duck;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.jirho.duckies.common.worldgen.DuckweedFeature;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Material;

public final class DuckiesRegistries {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Duckies.MOD_ID, Registry.BLOCK_REGISTRY);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Duckies.MOD_ID, Registry.ITEM_REGISTRY);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Duckies.MOD_ID, Registry.ENTITY_TYPE_REGISTRY);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Duckies.MOD_ID, Registry.SOUND_EVENT_REGISTRY);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Duckies.MOD_ID, Registry.FEATURE_REGISTRY);

    public static final RegistrySupplier<Block> DUCKWEED = BLOCKS.register("duckweed", () ->
            new DuckweedBlock(BlockBehaviour.Properties.of(Material.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)));

    public static final RegistrySupplier<Item> DUCKWEED_ITEM = ITEMS.register("duckweed", () ->
            new PlaceOnWaterBlockItem(DUCKWEED.get(), new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static final RegistrySupplier<EntityType<Duck>> DUCK = ENTITIES.register("duck", () ->
            EntityType.Builder.of(Duck::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.4F)
                    .build(new ResourceLocation(Duckies.MOD_ID, "duck").toString()));

    public static final RegistrySupplier<Item> DUCK_SPAWN_EGG = ITEMS.register("duck_spawn_egg", () ->
            new ArchitecturySpawnEggItem(DUCK, 15387438, 15557653, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistrySupplier<SoundEvent> DUCK_AMBIENT = SOUNDS.register("entity.duck.ambient", () ->
            new SoundEvent(new ResourceLocation(Duckies.MOD_ID, "entity.duck.ambient")));

    public static final RegistrySupplier<SoundEvent> DUCK_HURT = SOUNDS.register("entity.duck.hurt", () ->
            new SoundEvent(new ResourceLocation(Duckies.MOD_ID, "entity.duck.hurt")));

    public static final RegistrySupplier<SoundEvent> DUCK_DEATH = SOUNDS.register("entity.duck.death", () ->
            new SoundEvent(new ResourceLocation(Duckies.MOD_ID, "entity.duck.death")));

    public static final RegistrySupplier<SoundEvent> DUCK_ATTACK = SOUNDS.register("entity.duck.attack", () ->
            new SoundEvent(new ResourceLocation(Duckies.MOD_ID, "entity.duck.attack")));

    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> DUCKWEED_FEATURE = FEATURES.register("duckweed", DuckweedFeature::new);

    private DuckiesRegistries() {
    }

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
        ENTITIES.register();
        SOUNDS.register();
        FEATURES.register();

        EntityAttributeRegistry.register(DUCK, Duck::createAttributes);
    }
}
