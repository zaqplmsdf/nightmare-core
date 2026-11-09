package com.nightmarecore.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.nightmarecore.Config;
import com.nightmarecore.CurioSlots;
import com.nightmarecore.ModBalance;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.util.TextUtil;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * The core is now a pure curse item: it grants three blessing slots and applies
 * eight curses, but does not modify any attributes directly.
 */
public class NightmareCoreItem extends Item implements ICurioItem {

    public NightmareCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();

        if (CurioSlots.NIGHTMARE.equals(slotContext.identifier()) && !slotContext.cosmetic()) {
            CuriosApi.addSlotModifier(
                    modifiers, CurioSlots.BLESSING, uuid, ModBalance.BLESSING_SLOTS,
                    AttributeModifier.Operation.ADDITION);
        }
        return modifiers;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CurioSlots.NIGHTMARE.equals(slotContext.identifier()) && !slotContext.cosmetic();
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        // Survival players must use the Ember Blade. Creative mode remains the
        // admin escape hatch for testing and pack maintenance.
        return slotContext.entity() instanceof Player player && player.isCreative();
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltip, ItemStack stack) {
        return List.of();
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack previousStack, ItemStack stack) {
        if (slotContext.entity() instanceof ServerPlayer player) {
            player.displayClientMessage(
                    Component.translatable("message.nightmare_core.equipped").withStyle(ChatFormatting.DARK_PURPLE),
                    true);
            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.SOUL,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    20, 0.45D, 0.7D, 0.45D, 0.02D);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof ServerPlayer player) {
            player.displayClientMessage(
                    Component.translatable("message.nightmare_core.unequipped").withStyle(ChatFormatting.GRAY),
                    true);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof ServerPlayer player
                && player.tickCount % 100 == 0
                && !player.level().isClientSide) {
            player.serverLevel().sendParticles(ParticleTypes.SOUL,
                    player.getX(), player.getY() + 1.2D, player.getZ(),
                    1, 0.3D, 0.25D, 0.3D, 0.0D);
        }
    }

    @Override
    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source,
                                       int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return Config.KEEP_CORE_ON_DEATH.get()
                ? ICurio.DropRule.ALWAYS_KEEP
                : ICurio.DropRule.DEFAULT;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_NETHERITE, 1.0F, 0.65F);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.nightmare_core.core.curse")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.nightmare_core.core.slot")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.nightmare_core.core.blessing_slots")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.empty());

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.nightmarecore.client.ClientCurseTooltip.append(tooltip));

        tooltip.add(Component.translatable("tooltip.nightmare_core.core.curse_suppress")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.core.curse_lock")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.nightmare_core.core.fragment_header")
                .withStyle(ChatFormatting.GOLD));

        if (Config.ENABLE_FRAGMENT_DROPS.get()) {
            int min = Math.min(Config.MIN_FRAGMENTS.get(), Config.MAX_FRAGMENTS.get());
            int max = Math.max(Config.MIN_FRAGMENTS.get(), Config.MAX_FRAGMENTS.get());
            tooltip.add(Component.translatable("tooltip.nightmare_core.core.chance",
                            TextUtil.number(Config.BASE_CHANCE_PERCENT.get()),
                            TextUtil.number(Config.CHANCE_PER_LUCK_LEVEL.get()),
                            min, max)
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.nightmare_core.core.no_looting")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.nightmare_core.core.drops_disabled")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.nightmare_core.core.warning")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

        if (Config.KEEP_CORE_ON_DEATH.get()) {
            tooltip.add(Component.translatable("tooltip.nightmare_core.core.kept")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
