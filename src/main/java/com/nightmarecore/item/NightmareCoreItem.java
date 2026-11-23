package com.nightmarecore.item;

import com.nightmarecore.Config;
import com.nightmarecore.CurioSlots;
import com.nightmarecore.ModBalance;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.util.CurioUtil;
import com.nightmarecore.util.TextUtil;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
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
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/**
 * The core is a pure curse item: it unlocks the three blessing slots and
 * applies seven curses, but does not modify any attributes directly.
 */
public class NightmareCoreItem extends Item implements ICurioItem {

    public static final UUID BLESSING_LOCK_UUID =
            UUID.fromString("e5b1c2a4-7d6f-4b8a-9c31-2f4e8d6a7b90");
    public static final String BLESSING_LOCK_NAME = "nightmare_core:blessing_slots_locked";

    public NightmareCoreItem(Properties properties) {
        super(properties);
    }

    /**
     * The blessing slot type always has three base slots so Curios can
     * deserialize saved blessings safely during login/death rebuilds. While the
     * core is worn this method clears any temporary lock and legacy slot
     * modifier, restoring the three visible slots.
     */
    public static void ensureBlessingSlots(ServerPlayer player, ICuriosItemHandler handler) {
        handler.getStacksHandler(CurioSlots.BLESSING).ifPresent(stacks -> {
            stacks.clearModifiers();
            stacks.clearCachedModifiers();
            stacks.getSlots();
        });
        handler.processSlots();
        // Any stacks removed while shrinking legacy modifiers are handed back.
        handler.handleInvalidStacks();
    }

    /**
     * Hides the three blessing slots while no core is worn by adding a
     * temporary -3 modifier. Curios moves any equipped blessings back to the
     * player when the handler shrinks. The modifier is transient, so it is not
     * persisted: a save can always load the three base slots first.
     *
     * @return true if at least one blessing was equipped and has been ejected
     */
    public static boolean lockBlessingSlots(ICuriosItemHandler handler) {
        boolean[] hadItems = {false};
        boolean[] alreadyLocked = {false};
        handler.getStacksHandler(CurioSlots.BLESSING).ifPresent(stacks -> {
            if (stacks.getModifiers().containsKey(BLESSING_LOCK_UUID) && stacks.getSlots() <= 0) {
                // Already collapsed; keep the modifier untouched so Curios does
                // not rebuild it every tick check.
                alreadyLocked[0] = true;
                return;
            }
            for (int i = 0; i < stacks.getSlots(); i++) {
                if (!stacks.getStacks().getStackInSlot(i).isEmpty()) {
                    hadItems[0] = true;
                    break;
                }
            }
            stacks.clearModifiers();
            stacks.clearCachedModifiers();
            stacks.addTransientModifier(new AttributeModifier(BLESSING_LOCK_UUID,
                    BLESSING_LOCK_NAME, -ModBalance.BLESSING_SLOTS,
                    AttributeModifier.Operation.ADDITION));
            // Force the resize now so the slots disappear immediately.
            stacks.getSlots();
        });
        if (alreadyLocked[0]) {
            return false;
        }
        handler.processSlots();
        // Curios queues the stacks removed by the resize; give them back.
        handler.handleInvalidStacks();
        return hadItems[0];
    }

    /**
     * Applies the core-less state and notifies the player when blessings were
     * moved out of the now-hidden slots.
     */
    public static void handleCoreLost(ServerPlayer player, ICuriosItemHandler handler) {
        if (lockBlessingSlots(handler)) {
            player.displayClientMessage(
                    Component.translatable("message.nightmare_core.blessing.requires_core")
                            .withStyle(ChatFormatting.RED), true);
        }
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
        if (slotContext.entity() instanceof ServerPlayer player
                && CurioSlots.NIGHTMARE.equals(slotContext.identifier())
                && !slotContext.cosmetic()) {
            CuriosApi.getCuriosInventory(player)
                    .ifPresent(handler -> ensureBlessingSlots(player, handler));
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
            // Collapse the blessing slots on the next server tick: this method
            // can run inside Curios' container handling, so mutating another
            // slot handler here could disturb the menu that is being clicked.
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.execute(() -> CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                    if (!CurioUtil.hasNightmareCore(player)) {
                        handleCoreLost(player, handler);
                    }
                }));
            }
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
