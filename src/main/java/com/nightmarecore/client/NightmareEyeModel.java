package com.nightmarecore.client;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.entity.NightmareEntity;
import net.minecraft.client.model.EntityModel;
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

/**
 * Vanilla EntityModel conversion of 梦魇之眸.bbmodel. The idle / move / attack /
 * hurt / death animations are adapted from the supplied Blockbench animation
 * definitions without requiring GeckoLib.
 */
public class NightmareEyeModel extends EntityModel<NightmareEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(NightmareCore.MOD_ID, "nightmare_eye"), "main");

    private static final float TOP_BASE_X = -0.2182F;
    private static final float BOTTOM_BASE_X = 0.3054F;

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart body;
    private final ModelPart top;
    private final ModelPart bottom;
    private final ModelPart eye;
    private final ModelPart pupil;

    public NightmareEyeModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.body = this.bone.getChild("body");
        this.top = this.body.getChild("top");
        this.bottom = this.body.getChild("bottom");
        this.eye = this.body.getChild("eye");
        this.pupil = this.eye.getChild("pupil");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition bone = root.addOrReplaceChild("bone",
                CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 0.0F));
        PartDefinition body = bone.addOrReplaceChild("body",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("top", CubeListBuilder.create()
                        .texOffs(0, 46).addBox(-15.5F, 0.0F, -15.5F, 15.0F, 2.0F, 15.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 23).addBox(-16.0F, -7.0F, -16.0F, 16.0F, 7.0F, 16.0F, new CubeDeformation(0.0F))
                        .texOffs(64, 0).addBox(-15.0F, -8.0F, -15.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(8.0F, -8.0F, 8.0F, TOP_BASE_X, 0.0F, 0.0F));

        body.addOrReplaceChild("bottom", CubeListBuilder.create()
                        .texOffs(60, 46).addBox(-15.0F, -2.0F, -15.0F, 14.0F, 2.0F, 15.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 63).addBox(-15.0F, 7.0F, -15.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-16.0F, 0.0F, -16.0F, 16.0F, 7.0F, 16.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(8.0F, -8.0F, 8.0F, BOTTOM_BASE_X, 0.0F, 0.0F));

        PartDefinition eye = body.addOrReplaceChild("eye", CubeListBuilder.create()
                        .texOffs(0, 63).addBox(-6.0F, -14.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        eye.addOrReplaceChild("lid", CubeListBuilder.create()
                        .texOffs(64, 15).addBox(-16.0F, -2.0F, -1.0F, 17.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.5F, -7.0F, 7.0F, -0.6109F, 0.0F, 0.0F));
        eye.addOrReplaceChild("pupil", CubeListBuilder.create()
                        .texOffs(64, 19).addBox(-6.0F, -4.0F, -1.5F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -6.0F, -5.0F, 0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(NightmareEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        float idle = ageInTicks * 0.12F;
        float movement = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float attack = Mth.sin(Mth.clamp(entity.attackAnim, 0.0F, 1.0F) * (float) Math.PI);

        // Idle "待机": small body sway and vertical float.
        this.body.xRot = Mth.sin(idle) * 0.09F + 0.3927F * movement;
        this.body.yRot = Mth.sin(idle * 0.7F) * 0.16F;
        this.body.zRot = Mth.sin(idle * 1.3F) * 0.17F + Mth.sin(limbSwing) * 0.35F * movement;
        this.body.y = -4.0F + Mth.sin(idle) * 1.6F + Mth.sin(limbSwing * 2.0F) * 1.2F * movement;
        this.top.xRot = TOP_BASE_X + Mth.sin(idle * 1.1F) * 0.05F;
        this.bottom.xRot = BOTTOM_BASE_X - Mth.sin(idle * 1.1F) * 0.05F;
        this.eye.y = Mth.sin(idle * 0.8F) * 0.8F + 0.8F * movement;
        this.pupil.xRot = headPitch * 0.0174533F * 0.25F;
        this.pupil.yRot = netHeadYaw * 0.0174533F * 0.25F;

        // Attack "攻击": top and bottom snap open/close.
        this.top.xRot += 0.218F * attack;
        this.bottom.xRot -= 0.305F * attack;

        // Hurt "受伤": root recoil.
        if (entity.hurtTime > 0) {
            float hurt = entity.hurtTime / 10.0F;
            this.bone.xRot += 0.26F * hurt;
            this.bone.y = 17.0F - 1.0F * hurt;
        }

        // Death "死亡": shrink to nothing.
        if (entity.deathTime > 0) {
            float scale = Mth.clamp(1.0F - entity.deathTime / 20.0F, 0.0F, 1.0F);
            this.bone.xScale = scale;
            this.bone.yScale = scale;
            this.bone.zScale = scale;
        }
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
                               com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
