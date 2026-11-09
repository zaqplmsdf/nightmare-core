package com.nightmarecore.client;

import com.nightmarecore.entity.NightmareEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NightmareEyeGeoRenderer extends GeoEntityRenderer<NightmareEntity> {

    public NightmareEyeGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new NightmareEyeGeoModel());
        this.shadowRadius = 0.5F;
    }
}
