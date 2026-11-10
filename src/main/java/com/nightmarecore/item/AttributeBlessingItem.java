package com.nightmarecore.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.nightmarecore.CurioSlots;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.util.CurioUtil;
import com.nightmarecore.util.TextUtil;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * A special blessing made from a specific Nightmare's exclusive material.
 * These do not suppress curses; instead they provide attributes and a small
 * functional effect, competing with the seven suppression blessings for the
 * three blessing slots.
 */
public class AttributeBlessingItem extends Item implements ICurioItem {

    public record AttributeBonus(Attribute attribute, double amount) {
    }

    private final String id;
    private final List<AttributeBonus> bonuses;
    private final List<MobEffect> effects;

    public AttributeBlessingItem(String id, List<AttributeBonus> bonuses,
                                 List<MobEffect> effects, Properties properties) {
        super(properties);
        this.id = id;
        this.bonuses = List.copyOf(bonuses);
        this.effects = List.copyOf(effects);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        if (!CurioSlots.BLESSING.equals(slotContext.identifier()) || slotContext.cosmetic()) {
            return modifiers;
        }

        for (AttributeBonus bonus : this.bonuses) {
            var key = BuiltInRegistries.ATTRIBUTE.getKey(bonus.attribute());
            String suffix = key == null ? "attribute" : key.getPath();
            modifiers.put(bonus.attribute(), new AttributeModifier(
                    uuid, NightmareCore.MOD_ID + ":special_" + this.id + "_" + suffix,
                    bonus.amount(), AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CurioSlots.BLESSING.equals(slotContext.identifier())
                && !slotContext.cosmetic()
                && slotContext.entity() != null
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
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD, 1.0F, 1.25F);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltip, ItemStack stack) {
        return List.of();
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack previousStack, ItemStack stack) {
        applyEffects(slotContext.entity());
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            for (MobEffect effect : this.effects) {
                player.removeEffect(effect);
            }
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() != null
                && !slotContext.entity().level().isClientSide
                && slotContext.entity().tickCount % 20 == 0) {
            applyEffects(slotContext.entity());
        }
    }

    private void applyEffects(net.minecraft.world.entity.LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }
        for (MobEffect effect : this.effects) {
            entity.addEffect(new MobEffectInstance(effect, 80, 0, false, false, false));
        }
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
        for (AttributeBonus bonus : this.bonuses) {
            tooltip.add(Component.translatable("tooltip.nightmare_core.special_blessing.attribute",
                            TextUtil.number(bonus.amount()),
                            Component.translatable(bonus.attribute().getDescriptionId()))
                    .withStyle(ChatFormatting.BLUE));
        }
        for (MobEffect effect : this.effects) {
            tooltip.add(Component.translatable("tooltip.nightmare_core.special_blessing.effect",
                            effect.getDisplayName())
                    .withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(Component.translatable("tooltip.nightmare_core.special_blessing.no_suppress")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.special_blessing." + this.id)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.nightmare_core.blessing.unique")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
