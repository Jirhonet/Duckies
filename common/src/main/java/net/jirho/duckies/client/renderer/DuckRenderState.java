package net.jirho.duckies.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.DyeColor;

public class DuckRenderState extends LivingEntityRenderState {
    public boolean isSitting;
    public boolean onGround;
    public boolean isAngry;
    public boolean isTame;
    public DyeColor collarColor = DyeColor.RED;
}
