package net.jirho.duckies.common.item;

import net.jirho.duckies.common.block.DuckweedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class DuckweedBlockItem extends PlaceOnWaterBlockItem {
    public DuckweedBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return this.place(new BlockPlaceContext(context));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            return InteractionResult.PASS;
        }

        BlockPos surface = DuckweedBlock.findWaterColumnSurface(level, hit.getBlockPos());
        if (surface == null) {
            return InteractionResult.PASS;
        }

        BlockHitResult adjusted = new BlockHitResult(
                hit.getLocation(),
                Direction.DOWN,
                surface.above(),
                false);
        return this.useOn(new UseOnContext(player, hand, adjusted));
    }

    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        return DuckweedBlock.adjustPlacementContext(context);
    }
}
