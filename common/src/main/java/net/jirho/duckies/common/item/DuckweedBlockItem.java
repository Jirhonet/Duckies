package net.jirho.duckies.common.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public class DuckweedBlockItem extends PlaceOnWaterBlockItem {
    public DuckweedBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return this.place(new BlockPlaceContext(context));
    }
}
