package com.nightmarecore.registry;

import com.nightmarecore.NightmareCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NightmareCore.MOD_ID);

    public static final RegistryObject<SoundEvent> NIGHTMARE_EYE_GLARE = register("nightmare_eye_glare");
    public static final RegistryObject<SoundEvent> NIGHTMARE_EYE_BITE = register("nightmare_eye_bite");
    public static final RegistryObject<SoundEvent> NIGHTMARE_EYE_HURT = register("nightmare_eye_hurt");
    public static final RegistryObject<SoundEvent> NIGHTMARE_EYE_DEATH = register("nightmare_eye_death");
    public static final RegistryObject<SoundEvent> NIGHTMARE_EYE_IDLE = register("nightmare_eye_idle");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(NightmareCore.MOD_ID, name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private ModSounds() {
    }
}
