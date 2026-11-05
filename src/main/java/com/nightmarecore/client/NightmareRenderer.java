package com.nightmarecore.client;

import com.nightmarecore.entity.NightmareEntity;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Placeholder renderer: all five Nightmare types currently use the vanilla
 * zombie model. Each type is a separate entity type, so custom models and
 * textures can be attached per type later without changing the spawn logic.
 */
public class NightmareRenderer extends MobRenderer<NightmareEntity, ZombieModel<NightmareEntity>> {

    private static final ResourceLocation ZOMBIE_TEXTURE =
            new ResourceLocation("textures/entity/zombie/zombie.png");

    public NightmareRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(NightmareEntity entity) {
        return ZOMBIE_TEXTURE;
    }
}
