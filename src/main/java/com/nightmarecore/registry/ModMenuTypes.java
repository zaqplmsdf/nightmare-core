package com.nightmarecore.registry;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.menu.NightmareForgeMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, NightmareCore.MOD_ID);

    public static final RegistryObject<MenuType<NightmareForgeMenu>> NIGHTMARE_FORGE =
            MENUS.register("nightmare_forge", () -> IForgeMenuType.create(NightmareForgeMenu::new));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }

    private ModMenuTypes() {
    }
}
