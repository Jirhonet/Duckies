package net.jirho.duckies.common.block;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DuckweedBlock extends WaterlilyBlock implements BonemealableBlock {
    public static final int MAX_AMOUNT = 4;
    public static final IntegerProperty FLOWER_AMOUNT = IntegerProperty.create("flower_amount", 1, MAX_AMOUNT);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DuckweedBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FLOWER_AMOUNT, 1)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FLOWER_AMOUNT, FACING);
    }

    @Override
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        FluidState fluidAtPlacement = blockGetter.getFluidState(blockPos.above());
        if (fluidAtPlacement.getType() != Fluids.EMPTY) {
            return false;
        }

        FluidState fluidState = blockGetter.getFluidState(blockPos);
        if (fluidState.getType() == Fluids.WATER || blockState.getMaterial() == Material.ICE) {
            return true;
        }

        return Block.canSupportRigidBlock(blockGetter, blockPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level.getFluidState(pos).getType() == Fluids.WATER) {
            return false;
        }

        return super.canSurvive(state, level, pos);
    }

    public static BlockPos findWaterColumnSurface(BlockGetter level, BlockPos waterPos) {
        if (!level.getFluidState(waterPos).is(FluidTags.WATER)) {
            return null;
        }

        BlockPos surface = waterPos;
        while (level.getFluidState(surface.above()).is(FluidTags.WATER)) {
            surface = surface.above();
        }

        return surface;
    }

    public static BlockPlaceContext adjustPlacementContext(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState existing = level.getBlockState(pos);

        if (existing.getBlock() instanceof DuckweedBlock) {
            return context;
        }

        if (level.getFluidState(pos).is(FluidTags.WATER)) {
            BlockPos surface = findWaterColumnSurface(level, pos);
            if (surface == null) {
                return null;
            }

            BlockPos above = surface.above();
            if (level.getBlockState(above).canBeReplaced(context)) {
                return BlockPlaceContext.at(context, above, Direction.DOWN);
            }
            return null;
        }

        if (!level.getFluidState(pos).isEmpty()) {
            return null;
        }

        return context;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (level.getFluidState(pos).getType() == Fluids.WATER) {
            return null;
        }

        BlockState existing = level.getBlockState(pos);
        if (existing.is(this)) {
            return existing.setValue(FLOWER_AMOUNT, Math.min(MAX_AMOUNT, existing.getValue(FLOWER_AMOUNT) + 1));
        }

        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }

        return state
                .setValue(FLOWER_AMOUNT, 1)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive()
                && context.getItemInHand().is(this.asItem())
                && state.getValue(FLOWER_AMOUNT) < MAX_AMOUNT
                || this.material.isReplaceable()
                        && (context.getItemInHand().isEmpty() || !context.getItemInHand().is(this.asItem()));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(this.asItem()) && state.getValue(FLOWER_AMOUNT) < MAX_AMOUNT) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(FLOWER_AMOUNT, state.getValue(FLOWER_AMOUNT) + 1), 3);
                level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int amount = state.getValue(FLOWER_AMOUNT);
        Direction facing = state.getValue(FACING);
        VoxelShape shape = Shapes.empty();

        for (int patch = 1; patch <= amount; patch++) {
            shape = Shapes.or(shape, patch(patch, facing));
        }

        return shape;
    }

    private static VoxelShape patch(int patch, Direction facing) {
        double x1;
        double z1;
        double x2;
        double z2;

        switch (patch) {
            case 2 -> {
                x1 = 0.0;
                z1 = 8.0;
                x2 = 8.0;
                z2 = 16.0;
            }
            case 3 -> {
                x1 = 8.0;
                z1 = 8.0;
                x2 = 16.0;
                z2 = 16.0;
            }
            case 4 -> {
                x1 = 8.0;
                z1 = 0.0;
                x2 = 16.0;
                z2 = 8.0;
            }
            default -> {
                x1 = 0.0;
                z1 = 0.0;
                x2 = 8.0;
                z2 = 8.0;
            }
        }

        return rotateBox(x1, z1, x2, z2, facing);
    }

    private static VoxelShape rotateBox(double x1, double z1, double x2, double z2, Direction facing) {
        return switch (facing) {
            case EAST -> box(16.0 - z2, 0.0, x1, 16.0 - z1, 1.0, x2);
            case SOUTH -> box(16.0 - x2, 0.0, 16.0 - z2, 16.0 - x1, 1.0, 16.0 - z1);
            case WEST -> box(z1, 0.0, 16.0 - x2, z2, 1.0, 16.0 - x1);
            default -> box(x1, 0.0, z1, x2, 1.0, z2);
        };
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return type == PathComputationType.AIR && !this.hasCollision;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.singletonList(new ItemStack(this.asItem(), state.getValue(FLOWER_AMOUNT)));
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int amount = state.getValue(FLOWER_AMOUNT);
        if (amount < MAX_AMOUNT) {
            level.setBlock(pos, state.setValue(FLOWER_AMOUNT, amount + 1), 2);
        } else {
            popResource(level, pos, new ItemStack(this.asItem(), 1));
        }
    }
}
