package net.jirho.duckies.client.renderer;

import net.jirho.duckies.client.renderer.layers.DuckHeldItemLayer;
import net.jirho.duckies.client.renderer.model.DuckModel;
import net.jirho.duckies.common.entity.Duck;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DuckRenderer extends MobRenderer<Duck, DuckModel<Duck>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("duckies", "textures/entity/duck.png");

    public DuckRenderer(EntityRendererProvider.Context context) {
        super(context, new DuckModel<>(context.bakeLayer(DuckModel.LAYER_LOCATION)), 0.3F);
        this.addLayer(new DuckHeldItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Duck entity) {
        return TEXTURE;
    }
}
