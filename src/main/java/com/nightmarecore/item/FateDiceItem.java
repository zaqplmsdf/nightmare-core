package com.nightmarecore.item;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A high-end luck trinket with a 20% dodge chance on a short cooldown. The
 * dodge itself is handled in TrinketMechanicsHandler.
 */
public class FateDiceItem extends AttributeBlessingItem {

    public FateDiceItem(Properties properties) {
        super("fate_dice",
                List.of(new AttributeBonus(Attributes.LUCK, 2.0D),
                        new AttributeBonus(Attributes.MOVEMENT_SPEED, 0.01D)),
                List.of(), properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.nightmare_core.special_blessing.fate_dice.effect")
                .withStyle(ChatFormatting.GOLD));
    }
}
