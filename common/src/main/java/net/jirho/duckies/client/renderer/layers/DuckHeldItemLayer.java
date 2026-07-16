package net.jirho.duckies.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.jirho.duckies.client.renderer.DuckRenderState;
import net.jirho.duckies.client.renderer.model.DuckModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DuckHeldItemLayer extends RenderLayer<DuckRenderState, DuckModel> {
    private static final float BLOCKBENCH_ENTITY_HEIGHT = 24.0F;

    private final ItemRenderer itemRenderer;

    public DuckHeldItemLayer(RenderLayerParent<DuckRenderState, DuckModel> renderer, ItemRenderer itemRenderer) {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, DuckRenderState state,
            float yRot, float xRot) {
        ItemStack itemStack = state.getMainHandItem();
        BakedModel itemModel = state.getMainHandItemModel();
        if (itemStack.isEmpty() || itemModel == null) {
            return;
        }

        poseStack.pushPose();

        DuckModel model = this.getParentModel();
        float heldItemY = BLOCKBENCH_ENTITY_HEIGHT - model.heldItem.y;
        model.head.translateAndRotate(poseStack);
        poseStack.translate(
                (model.heldItem.x - model.head.x) / 16.0F,
                (heldItemY - model.head.y) / 16.0F,
                (model.heldItem.z - model.head.z) / 16.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        this.itemRenderer.render(itemStack, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, itemModel);
        poseStack.popPose();
    }
}
