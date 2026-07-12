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
            new ResourceLocation(Duckies.MOD_ID, "duckweed_river"));
    private static final ResourceKey<PlacedFeature> DUCKWEED_SWAMP = ResourceKey.create(
            Registries.PLACED_FEATURE,
            new ResourceLocation(Duckies.MOD_ID, "duckweed_swamp"));

    private DuckweedWorldGen() {
    }

    public static void init() {
        if (Platform.isNeoForge()) {
            return;
        }

        BiomeModifications.addProperties((ctx, mutable) -> {
            ctx.getKey().ifPresent(key -> {
                if (key.equals(new ResourceLocation("minecraft:river"))) {
                    mutable.getGenerationProperties().addFeature(
                            GenerationStep.Decoration.VEGETAL_DECORATION,
                            DUCKWEED_RIVER
                    );
                } else if (key.equals(new ResourceLocation("minecraft:swamp"))
                        || key.equals(new ResourceLocation("minecraft:mangrove_swamp"))) {
                    mutable.getGenerationProperties().addFeature(
                            GenerationStep.Decoration.VEGETAL_DECORATION,
                            DUCKWEED_SWAMP
                    );
                }
            });
        });
    }
}
