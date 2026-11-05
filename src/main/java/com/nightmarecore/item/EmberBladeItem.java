package com.nightmarecore.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.nightmarecore.CurioSlots;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.registry.ModItems;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * The only survival way to remove the nightmare core. It is also a functional
 * weapon with netherite-sword damage and 512 durability; cutting out the core
 * costs 64 durability and 6 magic damage to the wielder.
 */
public class EmberBladeItem extends Item {

    private static final float CUT_SELF_DAMAGE = 6.0F;
    private static final int CUT_DURABILITY_COST = 64;
    private static final UUID BLADE_DAMAGE_ID =
            UUID.nameUUIDFromBytes((NightmareCore.MOD_ID + ":ember_blade_damage").getBytes(StandardCharsets.UTF_8));
    private static final UUID BLADE_SPEED_ID =
            UUID.nameUUIDFromBytes((NightmareCore.MOD_ID + ":ember_blade_speed").getBytes(StandardCharsets.UTF_8));

    public EmberBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        modifiers.putAll(super.getDefaultAttributeModifiers(slot));

        if (slot == EquipmentSlot.MAINHAND) {
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    BLADE_DAMAGE_ID, NightmareCore.MOD_ID + ":ember_blade_damage", 7.0D,
                    AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(
                    BLADE_SPEED_ID, NightmareCore.MOD_ID + ":ember_blade_speed", -2.4D,
                    AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack blade = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(blade);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(blade);
        }
        if (serverPlayer.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(blade);
        }

        var inventory = CuriosApi.getCuriosInventory(serverPlayer).resolve().orElse(null);
        if (inventory == null) {
            return fail(serverPlayer, blade, "message.nightmare_core.ember_blade.no_core");
        }
        ICurioStacksHandler nightmare = inventory.getStacksHandler(CurioSlots.NIGHTMARE).orElse(null);
        if (nightmare == null || nightmare.getSlots() == 0
                || !nightmare.getStacks().getStackInSlot(0).is(ModItems.NIGHTMARE_CORE.get())) {
            return fail(serverPlayer, blade, "message.nightmare_core.ember_blade.no_core");
        }

        ItemStack core = nightmare.getStacks().getStackInSlot(0).copy();
        inventory.setEquippedCurio(CurioSlots.NIGHTMARE, 0, ItemStack.EMPTY);
        if (!serverPlayer.getInventory().add(core)) {
            serverPlayer.drop(core, false);
        }

        ServerLevel serverLevel = serverPlayer.serverLevel();
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(),
                28, 0.6D, 0.9D, 0.6D, 0.04D);
        serverLevel.playSound(null,
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.9F, 1.35F);
        serverPlayer.displayClientMessage(
                Component.translatable("message.nightmare_core.ember_blade.cut")
                        .withStyle(ChatFormatting.GOLD),
                false);
        // Removing the core tears the blade through the wielder as well.
        serverPlayer.hurt(serverPlayer.damageSources().magic(), CUT_SELF_DAMAGE);
        serverPlayer.getCooldowns().addCooldown(this, 20);
        blade.hurtAndBreak(CUT_DURABILITY_COST, serverPlayer, broken -> {
        });
        return InteractionResultHolder.consume(blade);
    }

    private static InteractionResultHolder<ItemStack> fail(ServerPlayer player, ItemStack blade, String key) {
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.GRAY), true);
        return InteractionResultHolder.fail(blade);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.nightmare_core.ember_blade.use")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.nightmare_core.ember_blade.weapon")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.ember_blade.cost")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.nightmare_core.ember_blade.flavor")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }
}
