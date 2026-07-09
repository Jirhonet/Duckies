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

public class DuckModel<T extends Duck> extends AgeableListModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("duckies", "duck"), "main");
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightpaw;
    private final ModelPart leftpaw;
    private final ModelPart fucku;

    public DuckModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightpaw = root.getChild("rightpaw");
        this.leftpaw = root.getChild("leftpaw");
        this.fucku = root.getChild("fucku");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(16, 0).addBox(-2.0F, -1.0F, -4.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, -3.0F));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 8).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(16, 3).addBox(-1.5F, -3.0F, 2.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.0F, 0.0F));
        partDefinition.addOrReplaceChild("rightpaw", CubeListBuilder.create().texOffs(17, 6).addBox(-0.5F, 0.0F, 0.25F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(16, 6).addBox(-0.5F, 1.0F, -0.75F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 23.0F, -0.25F));
        partDefinition.addOrReplaceChild("leftpaw", CubeListBuilder.create().texOffs(17, 5).addBox(-0.5F, 0.0F, 0.25F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(16, 5).addBox(-0.5F, 1.0F, -0.75F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 23.0F, -0.25F));
        partDefinition.addOrReplaceChild("fucku", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.fucku);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.head, this.body, this.rightpaw, this.leftpaw);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = headPitch * ((float) Math.PI / 180F);
        this.rightpaw.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }
}
