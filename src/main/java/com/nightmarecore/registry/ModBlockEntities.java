package com.nightmarecore.registry;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.block.NightmareForgeBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, NightmareCore.MOD_ID);

    public static final RegistryObject<BlockEntityType<NightmareForgeBlockEntity>> NIGHTMARE_FORGE =
            BLOCK_ENTITIES.register("nightmare_forge", () -> BlockEntityType.Builder
                    .of(NightmareForgeBlockEntity::new, ModBlocks.NIGHTMARE_FORGE.get())
                    .build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }

    private ModBlockEntities() {
    }
}
