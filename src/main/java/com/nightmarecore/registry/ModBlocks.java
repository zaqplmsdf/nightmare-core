package com.nightmarecore.registry;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.block.NightmareForgeBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {

    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, NightmareCore.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NightmareCore.MOD_ID);

    public static final RegistryObject<NightmareForgeBlock> NIGHTMARE_FORGE =
            BLOCKS.register("nightmare_forge", () -> new NightmareForgeBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DEEPSLATE)
                            .strength(3.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    public static final RegistryObject<Item> NIGHTMARE_FORGE_ITEM =
            ITEMS.register("nightmare_forge", () -> new BlockItem(
                    NIGHTMARE_FORGE.get(), new Item.Properties().stacksTo(64)));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    private ModBlocks() {
    }
}
