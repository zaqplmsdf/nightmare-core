package com.nightmarecore.item;

import com.nightmarecore.CurseType;
import com.nightmarecore.CurioSlots;
import com.nightmarecore.util.CurioUtil;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * A blessing trinket. Blessings no longer grant attributes; each one suppresses
 * exactly one of the core's eight curses.
 */
public class BlessingItem extends Item implements ICurioItem {

    private final String id;

    public BlessingItem(String id, Properties properties) {
        super(properties);
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CurioSlots.BLESSING.equals(slotContext.identifier())
                && !slotContext.cosmetic()
                && slotContext.entity() != null
                && CurioUtil.hasNightmareCore(slotContext.entity())
                && !CurioUtil.isDuplicateBlessing(slotContext, stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD, 1.0F, 1.55F);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltip, ItemStack stack) {
        return List.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.nightmare_core.blessing.slot")
                .withStyle(ChatFormatting.DARK_PURPLE));
        CurseType.byBlessing(this).ifPresent(curse ->
                tooltip.add(Component.translatable("tooltip.nightmare_core.blessing.suppresses",
                                Component.translatable(curse.translationKey()))
                        .withStyle(ChatFormatting.GREEN)));
        tooltip.add(Component.translatable("tooltip.nightmare_core.blessing." + this.id)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.nightmare_core.blessing.unique")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.blessing.note")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
