package com.nightmarecore.item;

import com.nightmarecore.Config;
import com.nightmarecore.CurioSlots;
import com.nightmarecore.CurseType;
import com.nightmarecore.util.CurioUtil;
import com.nightmarecore.util.TextUtil;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * A utility blessing: no attributes, but it adds a flat chance to nightmare
 * fragment rolls. It occupies one of the three blessing slots, so choosing it
 * means giving up a stat blessing.
 */
public class DreamhunterEyeItem extends Item implements ICurioItem {

    public DreamhunterEyeItem(Properties properties) {
        super(properties);
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
    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source,
                                       int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return BlessingItem.keepOnDeath(slotContext)
                ? ICurio.DropRule.ALWAYS_KEEP : ICurio.DropRule.DEFAULT;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.AMETHYST_BLOCK_CHIME, 0.9F, 1.45F);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.nightmare_core.blessing.slot")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.nightmare_core.charm.effect",
                        TextUtil.number(Config.CHARM_BONUS_PERCENT.get()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.charm.no_looting")
                .withStyle(ChatFormatting.DARK_GRAY));
        CurseType.byBlessing(this).ifPresent(curse ->
                tooltip.add(Component.translatable("tooltip.nightmare_core.blessing.suppresses",
                                Component.translatable(curse.translationKey()))
                        .withStyle(ChatFormatting.GREEN)));
        tooltip.add(Component.translatable("tooltip.nightmare_core.blessing.unique")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.charm.flavor")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
