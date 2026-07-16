package net.jirho.duckies.client.renderer;

import net.jirho.duckies.client.renderer.layers.DuckAngryLayer;
import net.jirho.duckies.client.renderer.layers.DuckColorLayer;
import net.jirho.duckies.client.renderer.layers.DuckHeldItemLayer;
import net.jirho.duckies.client.renderer.model.DuckModel;
import net.jirho.duckies.common.entity.Duck;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DuckRenderer extends AgeableMobRenderer<Duck, DuckRenderState, DuckModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("duckies", "textures/entity/duck.png");

    public DuckRenderer(EntityRendererProvider.Context context) {
        super(context, new DuckModel(context.bakeLayer(DuckModel.LAYER_LOCATION)),
                new DuckModel(context.bakeLayer(DuckModel.BABY_LAYER_LOCATION)), 0.3F);
        this.addLayer(new DuckAngryLayer(this));
        this.addLayer(new DuckColorLayer(this));
        this.addLayer(new DuckHeldItemLayer(this, context.getItemRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(DuckRenderState state) {
        return TEXTURE;
    }

    @Override
    public DuckRenderState createRenderState() {
        return new DuckRenderState();
    }

    @Override
    public void extractRenderState(Duck duck, DuckRenderState state, float partialTick) {
        super.extractRenderState(duck, state, partialTick);
        state.isSitting = duck.isInSittingPose();
        state.onGround = duck.onGround();
        state.isAngry = duck.isAngry();
        state.isTame = duck.isTame();
        state.collarColor = duck.getColor();
    }
}
