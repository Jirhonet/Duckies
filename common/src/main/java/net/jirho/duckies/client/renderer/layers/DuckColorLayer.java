package net.jirho.duckies.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.jirho.duckies.client.renderer.DuckRenderState;
import net.jirho.duckies.client.renderer.model.DuckModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class DuckColorLayer extends RenderLayer<DuckRenderState, DuckModel> {
    private static final ResourceLocation COLOR_TEXTURE = ResourceLocation.fromNamespaceAndPath("duckies",
            "textures/entity/duck_color.png");

    public DuckColorLayer(RenderLayerParent<DuckRenderState, DuckModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, DuckRenderState state,
            float yRot, float xRot) {
        if (state.isInvisible || !state.isTame) {
            return;
        }

        int color = state.collarColor.getTextureDiffuseColor();
        RenderLayer.renderColoredCutoutModel(this.getParentModel(), COLOR_TEXTURE, poseStack, buffer, packedLight,
                state, color);
    }
}
