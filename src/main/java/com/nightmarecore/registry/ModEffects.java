package com.nightmarecore.registry;

import com.nightmarecore.NightmareCore;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, NightmareCore.MOD_ID);

    public static final RegistryObject<MobEffect> INSANITY = EFFECTS.register("insanity",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x6C3A8A) {
            });

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }

    private ModEffects() {
    }
}
