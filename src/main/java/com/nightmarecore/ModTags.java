package com.nightmarecore;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public final class ModTags {

    public static final TagKey<Block> NO_FRAGMENT_DROP_BLOCKS =
            BlockTags.create(new ResourceLocation(NightmareCore.MOD_ID, "no_fragment_drops"));
    public static final TagKey<EntityType<?>> NO_FRAGMENT_DROP_ENTITIES =
            TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation(NightmareCore.MOD_ID, "no_fragment_drops"));

    private ModTags() {
    }
}
