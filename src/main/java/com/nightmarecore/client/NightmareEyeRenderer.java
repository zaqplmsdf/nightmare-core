package com.nightmarecore.client;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.entity.NightmareEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the custom Nightmare Eye model. Animations are handled in
 * {@link NightmareEyeModel#setupAnim}.
 */
public class NightmareEyeRenderer extends MobRenderer<NightmareEntity, NightmareEyeModel> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NightmareCore.MOD_ID, "textures/entity/nightmare_eye.png");

    public NightmareEyeRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareEyeModel(context.bakeLayer(NightmareEyeModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(NightmareEntity entity) {
        return TEXTURE;
    }
}
