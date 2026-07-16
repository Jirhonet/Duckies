package net.jirho.duckies.client.renderer;

import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.world.item.DyeColor;

public class DuckRenderState extends HoldingEntityRenderState {
    public boolean isSitting;
    public boolean onGround;
    public boolean isAngry;
    public boolean isTame;
    public DyeColor collarColor = DyeColor.RED;
}
