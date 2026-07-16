package net.jirho.duckies.init;

import dev.architectury.platform.Platform;
import dev.architectury.registry.level.biome.BiomeModifications;
import net.jirho.duckies.Duckies;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class DuckweedWorldGen {
    private static final ResourceKey<PlacedFeature> DUCKWEED_RIVER = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Duckies.MOD_ID, "duckweed_river"));
    private static final ResourceKey<PlacedFeature> DUCKWEED_SWAMP = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Duckies.MOD_ID, "duckweed_swamp"));
    private static final ResourceLocation RIVER = ResourceLocation.parse("minecraft:river");
    private static final ResourceLocation SWAMP = ResourceLocation.parse("minecraft:swamp");
    private static final ResourceLocation MANGROVE_SWAMP = ResourceLocation.parse("minecraft:mangrove_swamp");

    private DuckweedWorldGen() {
    }

    public static void init() {
        if (Platform.isNeoForge()) {
            return;
        }

        BiomeModifications.addProperties((ctx, mutable) -> {
            ctx.getKey().ifPresent(key -> {
                if (key.equals(RIVER)) {
                    mutable.getGenerationProperties().addFeature(
                            GenerationStep.Decoration.VEGETAL_DECORATION,
                            DUCKWEED_RIVER);
                } else if (key.equals(SWAMP) || key.equals(MANGROVE_SWAMP)) {
                    mutable.getGenerationProperties().addFeature(
                            GenerationStep.Decoration.VEGETAL_DECORATION,
                            DUCKWEED_SWAMP);
                }
            });
        });
    }
}
