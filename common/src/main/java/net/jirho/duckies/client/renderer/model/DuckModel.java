package net.jirho.duckies.client.renderer.model;

import com.google.common.collect.ImmutableList;

import net.jirho.duckies.common.entity.Duck;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.TamableAnimal;

public class DuckModel<T extends Duck> extends AgeableListModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation("duckies", "duck"), "main");
    public static final float BABY_HEAD_SCALE = 2.0F;
    public static final float BABY_Y_HEAD_OFFSET = 9.7F;
    public static final float BABY_Z_HEAD_OFFSET = 1.0F;
    private static final float FALLING_FOOT_SWING_SPEED = 1.75F;

    public final ModelPart head;
    public final ModelPart heldItem;
    private final ModelPart body;
    private final ModelPart feet;
    private final ModelPart leftFoot;
    private final ModelPart rightFoot;

    public DuckModel(ModelPart root) {
        super(true, BABY_Y_HEAD_OFFSET, BABY_Z_HEAD_OFFSET, BABY_HEAD_SCALE, 2.0F, 24.0F);
        this.head = root.getChild("head");
        this.heldItem = root.getChild("held_item");
        this.body = root.getChild("body");
        this.feet = root.getChild("feet");
        this.leftFoot = this.feet.getChild("left_foot");
        this.rightFoot = this.feet.getChild("right_foot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -4.0F, -2.5F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 0).addBox(-2.0F, -1.0F, -4.5F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 19.0F, -2.5F));
        partDefinition.addOrReplaceChild("held_item", CubeListBuilder.create(),
                PartPose.offset(0.0F, 5.5F, -5.0F));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 8).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(16, 3).addBox(-1.5F, -3.0F, 2.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 21.0F, 0.0F));
        PartDefinition feet = partDefinition.addOrReplaceChild("feet", CubeListBuilder.create(),
                PartPose.offset(0.0F, 23.0F, 0.5F));
        feet.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(16, 5).addBox(0.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 0.0F, 0.0F));
        feet.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(16, 5).addBox(0.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.feet);
    }

    @Override
    public void prepareMobModel(T arg, float f, float g, float h) {
        if (((TamableAnimal) arg).isInSittingPose()) {
            this.body.setPos(0.0F, 22.0F, 0.0F);
            this.feet.setPos(0.0F, 24.0F, 0.5F);
            this.head.setPos(0.0F, 20.0F, -2.5F);
            this.heldItem.setPos(0.0F, 4.5F, -5.0F);
        } else {
            this.body.setPos(0.0F, 21.0F, 0.0F);
            this.feet.setPos(0.0F, 23.0F, 0.5F);
            this.head.setPos(0.0F, 19.0F, -2.5F);
            this.heldItem.setPos(0.0F, 5.5F, -5.0F);
        }
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch) {
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);

        float footSwing;
        float footSwingAmount;
        if (this.isFalling(entity)) {
            footSwing = ageInTicks * FALLING_FOOT_SWING_SPEED;
            footSwingAmount = 1.0F;
        } else {
            footSwing = limbSwing;
            footSwingAmount = limbSwingAmount;
        }

        this.leftFoot.xRot = Mth.cos(footSwing * 0.6662F) * 1.4F * footSwingAmount;
        this.rightFoot.xRot = Mth.cos(footSwing * 0.6662F + (float) Math.PI) * 1.4F * footSwingAmount;
    }

    private boolean isFalling(T entity) {
        return !entity.onGround()
                && !entity.isInSittingPose();
    }
}
