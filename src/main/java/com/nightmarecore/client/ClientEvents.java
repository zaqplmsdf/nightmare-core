package com.nightmarecore.client;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModEntities.ALL.stream()
                .filter(type -> type != ModEntities.RIFT_NIGHTMARE)
                .forEach(type -> event.registerEntityRenderer(type.get(), NightmareRenderer::new));
        event.registerEntityRenderer(ModEntities.RIFT_NIGHTMARE.get(), NightmareEyeGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.NIGHTMARE_PROJECTILE.get(), NightmareProjectileRenderer::new);
    }

    private ClientEvents() {
    }
}
