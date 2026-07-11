package net.jirho.duckies.init;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.level.biome.BiomeModifications;
import net.jirho.duckies.Duckies;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

import java.util.List;

public final class DuckweedWorldGen {
    private DuckweedWorldGen() {
    }

    public static void init() {
        LifecycleEvent.SETUP.register(() -> {
            Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> configuredFeature = FeatureUtils.register(
                    Duckies.MOD_ID + ":duckweed",
                    DuckiesRegistries.DUCKWEED_FEATURE.get(),
                    NoneFeatureConfiguration.INSTANCE
            );

            Holder<PlacedFeature> riverPlacedFeature = PlacementUtils.register(
                    Duckies.MOD_ID + ":duckweed_river",
                    configuredFeature,
                    List.of(
                            CountPlacement.of(24),
                            InSquarePlacement.spread(),
                            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                            SurfaceWaterDepthFilter.forMaxDepth(2),
                            BiomeFilter.biome()
                    )
            );

            Holder<PlacedFeature> swampPlacedFeature = PlacementUtils.register(
                    Duckies.MOD_ID + ":duckweed_swamp",
                    configuredFeature,
                    List.of(
                            CountPlacement.of(8),
                            InSquarePlacement.spread(),
                            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                            BiomeFilter.biome()
                    )
            );

            BiomeModifications.addProperties((ctx, mutable) -> {
                ctx.getKey().ifPresent(key -> {
                    if (key.equals(new ResourceLocation("minecraft:river"))) {
                        mutable.getGenerationProperties().addFeature(
                                GenerationStep.Decoration.VEGETAL_DECORATION,
                                riverPlacedFeature
                        );
                    } else if (key.equals(new ResourceLocation("minecraft:swamp"))
                            || key.equals(new ResourceLocation("minecraft:mangrove_swamp"))) {
                        mutable.getGenerationProperties().addFeature(
                                GenerationStep.Decoration.VEGETAL_DECORATION,
                                swampPlacedFeature
                        );
                    }
                });
            });
        });
    }
}
