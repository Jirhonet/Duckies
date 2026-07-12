package net.jirho.duckies.common.item;

import net.jirho.duckies.common.block.DuckweedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        BlockPos surface = DuckweedBlock.findWaterColumnSurface(level, hit.getBlockPos());
        if (surface == null) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        BlockHitResult adjusted = new BlockHitResult(
                hit.getLocation(),
                Direction.DOWN,
                surface.above(),
                false);
        InteractionResult result = this.useOn(new UseOnContext(player, hand, adjusted));
        return new InteractionResultHolder<>(result, player.getItemInHand(hand));
    }

    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        return DuckweedBlock.adjustPlacementContext(context);
    }
}
