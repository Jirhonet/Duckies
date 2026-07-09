package net.jirho.duckies.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.jirho.duckies.client.renderer.model.DuckModel;
import net.jirho.duckies.common.entity.Duck;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class DuckHeldItemLayer extends RenderLayer<Duck, DuckModel<Duck>> {
    private static final float BLOCKBENCH_ENTITY_HEIGHT = 24.0F;

    private final ItemInHandRenderer itemInHandRenderer;

    public DuckHeldItemLayer(RenderLayerParent<Duck, DuckModel<Duck>> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Duck duck, float limbSwing,
            float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemStack = duck.getItemBySlot(EquipmentSlot.MAINHAND);
        if (itemStack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        if (duck.isBaby()) {
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(0.0D, 0.5D, 0.209375D);
        }

        DuckModel<Duck> model = this.getParentModel();
        float heldItemY = BLOCKBENCH_ENTITY_HEIGHT - model.heldItem.y;
        model.head.translateAndRotate(poseStack);
        poseStack.translate(
                (model.heldItem.x - model.head.x) / 16.0F,
                (heldItemY - model.head.y) / 16.0F,
                (model.heldItem.z - model.head.z) / 16.0F);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));

        this.itemInHandRenderer.renderItem(duck, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack,
                buffer, packedLight);
        poseStack.popPose();
    }
}
