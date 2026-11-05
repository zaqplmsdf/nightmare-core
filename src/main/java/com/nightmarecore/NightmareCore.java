package com.nightmarecore;

import com.mojang.logging.LogUtils;
import com.nightmarecore.registry.ModCreativeTabs;
import com.nightmarecore.registry.ModEffects;
import com.nightmarecore.registry.ModEntities;
import com.nightmarecore.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Nightmare Core: a deliberately negative curio that opens three blessing slots.
 */
@Mod(NightmareCore.MOD_ID)
public final class NightmareCore {

    public static final String MOD_ID = "nightmare_core";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NightmareCore() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modBus);
        ModCreativeTabs.register(modBus);
        ModEffects.register(modBus);
        ModEntities.register(modBus);
        modBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModBalance::verify);
    }
}
