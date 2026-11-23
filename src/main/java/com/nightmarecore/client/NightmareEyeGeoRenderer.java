package com.nightmarecore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nightmarecore.entity.NightmareEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NightmareEyeGeoRenderer extends GeoEntityRenderer<NightmareEntity> {

    /**
     * The animated eyeball floats slightly above its 0.8-block hitbox; lower the
     * rendered model by 0.3 blocks so it visually sits inside the entity.
     */
    private static final double MODEL_Y_OFFSET = -0.3D;

    public NightmareEyeGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareEyeGeoModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public void preRender(PoseStack poseStack, NightmareEntity animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        if (!isReRender) {
            poseStack.translate(0.0D, MODEL_Y_OFFSET, 0.0D);
        }
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
