package net.jirho.duckies.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class DuckiesTags {
    public static final TagKey<Item> DUCK_EDIBLE = TagKey.create(Registry.ITEM_REGISTRY,
            new ResourceLocation("duckies", "duck_edible"));

    private DuckiesTags() {
    }
}
