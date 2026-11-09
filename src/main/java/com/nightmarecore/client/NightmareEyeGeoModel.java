package com.nightmarecore.client;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.entity.NightmareEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NightmareEyeGeoModel extends GeoModel<NightmareEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(NightmareCore.MOD_ID, "geo/nightmare_eye.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NightmareCore.MOD_ID, "textures/entity/nightmare_eye.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(NightmareCore.MOD_ID, "animations/nightmare_eye.animation.json");

    @Override
    public ResourceLocation getModelResource(NightmareEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(NightmareEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(NightmareEntity animatable) {
        return ANIMATION;
    }
}
