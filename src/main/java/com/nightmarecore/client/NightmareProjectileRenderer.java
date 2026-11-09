package com.nightmarecore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.entity.NightmareProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class NightmareProjectileRenderer extends EntityRenderer<NightmareProjectile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NightmareCore.MOD_ID, "textures/entity/nightmare_projectile.png");

    public NightmareProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(NightmareProjectile entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float size = 0.35F;
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        consumer.vertex(pose.pose(), -size, -size, 0.0F)
                .color(255, 255, 255, 255).uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), size, -size, 0.0F)
                .color(255, 255, 255, 255).uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), size, size, 0.0F)
                .color(255, 255, 255, 255).uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), -size, size, 0.0F)
                .color(255, 255, 255, 255).uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(NightmareProjectile entity) {
        return TEXTURE;
    }
}
