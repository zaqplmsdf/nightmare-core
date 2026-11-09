package com.nightmarecore.client;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.registry.ModEntities;
import com.nightmarecore.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

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

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(
                ModMenuTypes.NIGHTMARE_FORGE.get(), NightmareForgeScreen::new));
    }

    private ClientEvents() {
    }
}
