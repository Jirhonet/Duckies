package net.jirho.duckies.common.worldgen;

import net.minecraft.core.registries.Registries;
import net.jirho.duckies.common.block.DuckweedBlock;
import net.jirho.duckies.init.DuckiesRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DuckweedFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceKey<Biome> RIVER = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.parse("minecraft:river"));
    private static final ResourceKey<Biome> SWAMP = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.parse("minecraft:swamp"));
    private static final ResourceKey<Biome> MANGROVE_SWAMP = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.parse("minecraft:mangrove_swamp"));
    private static final int MAX_SHORE_DISTANCE = 8;
    private static final int RIVER_SURFACE_SEARCH_RANGE = 4;
    private static final int RIVER_PATCH_SPREAD = 5;
    private static final int SWAMP_SPREAD_XZ = 7;
    private static final int SWAMP_SPREAD_Y = 3;
    private static final int SWAMP_SURFACE_SEARCH_RANGE = 4;
    private static final int SWAMP_ATTEMPTS_PER_PLACEMENT = 10;

    public DuckweedFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        if (isSwampBiome(level, origin)) {
            return placeSwamp(level, origin, random);
        }

        return placeRiver(level, origin, random);
    }

    private static boolean placeSwamp(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockState duckweed = DuckiesRegistries.DUCKWEED.get().defaultBlockState();
        boolean placedAny = false;

        for (int attempt = 0; attempt < SWAMP_ATTEMPTS_PER_PLACEMENT; attempt++) {
            BlockPos searchPos = origin.offset(
                    random.nextInt(SWAMP_SPREAD_XZ * 2 + 1) - SWAMP_SPREAD_XZ,
                    random.nextInt(SWAMP_SPREAD_Y * 2 + 1) - SWAMP_SPREAD_Y,
                    random.nextInt(SWAMP_SPREAD_XZ * 2 + 1) - SWAMP_SPREAD_XZ);

            BlockPos pos = findWaterSurface(level, searchPos, SWAMP_SURFACE_SEARCH_RANGE);
            if (pos == null || !isSwampBiome(level, pos)) {
                continue;
            }

            BlockState state = duckweed
                    .setValue(DuckweedBlock.FLOWER_AMOUNT, 1 + random.nextInt(DuckweedBlock.MAX_AMOUNT))
                    .setValue(DuckweedBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
            level.setBlock(pos, state, 2);
            placedAny = true;
        }

        return placedAny;
    }

    private static boolean placeRiver(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos anchor = findWaterSurface(level, origin, RIVER_SURFACE_SEARCH_RANGE);
        if (anchor == null || !isNearRiverEdge(level, anchor)) {
            return false;
        }

        BlockState duckweed = DuckiesRegistries.DUCKWEED.get().defaultBlockState();
        boolean placedAny = false;
        int patchSize = 8 + random.nextInt(9);

        for (int attempt = 0; attempt < patchSize; attempt++) {
            BlockPos searchPos = anchor.offset(
                    random.nextInt(RIVER_PATCH_SPREAD * 2 + 1) - RIVER_PATCH_SPREAD,
                    0,
                    random.nextInt(RIVER_PATCH_SPREAD * 2 + 1) - RIVER_PATCH_SPREAD);
            BlockPos pos = findWaterSurface(level, searchPos, RIVER_SURFACE_SEARCH_RANGE);

            if (pos == null || !isNearRiverEdge(level, pos)) {
                continue;
            }

            BlockState state = duckweed
                    .setValue(DuckweedBlock.FLOWER_AMOUNT, 1 + random.nextInt(DuckweedBlock.MAX_AMOUNT))
                    .setValue(DuckweedBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
            level.setBlock(pos, state, 2);
            placedAny = true;
        }

        return placedAny;
    }

    private static boolean isSwampBiome(WorldGenLevel level, BlockPos placePos) {
        return level.getBiome(placePos.below()).unwrapKey().map(DuckweedFeature::isSwampBiome).orElse(false);
    }

    private static boolean isSwampBiome(ResourceKey<Biome> biome) {
        return biome.equals(SWAMP) || biome.equals(MANGROVE_SWAMP);
    }

    private static boolean isNearRiverEdge(WorldGenLevel level, BlockPos placePos) {
        BlockPos waterPos = placePos.below();
        if (!level.getBiome(waterPos).unwrapKey().filter(RIVER::equals).isPresent()) {
            return false;
        }

        for (int dx = -MAX_SHORE_DISTANCE; dx <= MAX_SHORE_DISTANCE; dx++) {
            for (int dz = -MAX_SHORE_DISTANCE; dz <= MAX_SHORE_DISTANCE; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }

                BlockPos sample = waterPos.offset(dx, 0, dz);
                if (isShoreOrBiomeBoundary(level, sample)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isShoreOrBiomeBoundary(WorldGenLevel level, BlockPos sample) {
        if (!level.getBiome(sample).unwrapKey().filter(RIVER::equals).isPresent()) {
            return true;
        }

        for (int dy = 0; dy <= 4; dy++) {
            BlockPos check = sample.above(dy);
            BlockState state = level.getBlockState(check);

            if (state.isAir()) {
                continue;
            }

            if (state.getFluidState().is(FluidTags.WATER)) {
                break;
            }

            if (state.isSolidRender()) {
                return true;
            }
        }

        return false;
    }

    private static BlockPos findWaterSurface(WorldGenLevel level, BlockPos origin, int verticalRange) {
        BlockPos.MutableBlockPos pos = origin.mutable();

        for (int dy = -verticalRange; dy <= verticalRange; dy++) {
            pos.set(origin.getX(), origin.getY() + dy, origin.getZ());
            if (canPlaceAt(level, pos)) {
                return pos.immutable();
            }
        }

        return null;
    }

    private static boolean canPlaceAt(WorldGenLevel level, BlockPos placePos) {
        BlockState existing = level.getBlockState(placePos);
        if (existing.is(DuckiesRegistries.DUCKWEED.get())) {
            return existing.getValue(DuckweedBlock.FLOWER_AMOUNT) < DuckweedBlock.MAX_AMOUNT;
        }

        return existing.isAir() && level.getFluidState(placePos.below()).is(FluidTags.WATER);
    }
}
