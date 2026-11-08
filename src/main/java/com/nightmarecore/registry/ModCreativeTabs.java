package com.nightmarecore.registry;

import com.nightmarecore.NightmareCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NightmareCore.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nightmare_core"))
                    .icon(() -> new ItemStack(ModItems.NIGHTMARE_CORE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.NIGHTMARE_CORE.get());
                        output.accept(ModItems.NIGHTMARE_FRAGMENT.get());
                        output.accept(ModItems.ASHEN_CINDER.get());
                        output.accept(ModItems.TIDAL_TEAR.get());
                        output.accept(ModItems.RIFT_SHARD.get());
                        output.accept(ModItems.VIGIL_EYE.get());
                        output.accept(ModItems.HOLLOW_SHELL.get());
                        output.accept(ModItems.EMBER_HEART.get());
                        output.accept(ModItems.DREAMREND_FANG.get());
                        output.accept(ModItems.FLEET_FEATHER.get());
                        output.accept(ModItems.VIGIL_CARAPACE.get());
                        output.accept(ModItems.STONEBOUND_PENDANT.get());
                        output.accept(ModItems.ECHO_BELL.get());
                        output.accept(ModItems.DREAMHUNTER_EYE.get());
                        output.accept(ModItems.EMBER_CROWN.get());
                        output.accept(ModItems.TIDAL_PENDANT.get());
                        output.accept(ModItems.RIFT_EYE.get());
                        output.accept(ModItems.VIGIL_LANTERN.get());
                        output.accept(ModItems.HOLLOW_CHARM.get());
                        output.accept(ModItems.ABYSSAL_CROWN.get());
                        output.accept(ModItems.VOID_GAUNTLET.get());
                        output.accept(ModItems.FATE_DICE.get());
                        output.accept(ModItems.NIGHTMARE_PRISM.get());
                        output.accept(ModItems.DREAM_ANCHOR.get());
                        output.accept(ModItems.EMBER_BLADE.get());
                        output.accept(ModItems.BLESSING_BAG.get());
                        output.accept(ModItems.ASHEN_NIGHTMARE_SPAWN_EGG.get());
                        output.accept(ModItems.TIDAL_NIGHTMARE_SPAWN_EGG.get());
                        output.accept(ModItems.RIFT_NIGHTMARE_SPAWN_EGG.get());
                        output.accept(ModItems.VIGIL_NIGHTMARE_SPAWN_EGG.get());
                        output.accept(ModItems.HOLLOW_NIGHTMARE_SPAWN_EGG.get());
                    })
                    .build());

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }

    private ModCreativeTabs() {
    }
}
