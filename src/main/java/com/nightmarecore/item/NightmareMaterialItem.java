package com.nightmarecore.item;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Nightmare-exclusive crafting material. Tooltip names the variant that drops it.
 */
public class NightmareMaterialItem extends Item {

    private final String id;

    public NightmareMaterialItem(String id, Properties properties) {
        super(properties);
        this.id = id;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.nightmare_core.material." + this.id)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
