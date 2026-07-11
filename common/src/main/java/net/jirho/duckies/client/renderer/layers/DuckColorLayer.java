package net.jirho.duckies.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.jirho.duckies.client.renderer.model.DuckModel;
import net.jirho.duckies.common.entity.Duck;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class DuckColorLayer extends RenderLayer<Duck, DuckModel<Duck>> {
    private static final ResourceLocation COLOR_TEXTURE = new ResourceLocation("duckies",
            "textures/entity/duck_color.png");

    public DuckColorLayer(RenderLayerParent<Duck, DuckModel<Duck>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Duck duck, float limbSwing,
            float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (duck.isInvisible() || !duck.isTame()) {
            return;
        }

        float[] colors = duck.getColor().getTextureDiffuseColors();
        RenderLayer.renderColoredCutoutModel(this.getParentModel(), COLOR_TEXTURE, poseStack, buffer, packedLight,
                duck,
                colors[0], colors[1], colors[2]);
    }
}
