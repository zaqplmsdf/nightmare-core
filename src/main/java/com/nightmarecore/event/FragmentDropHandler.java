package com.nightmarecore.event;

import com.nightmarecore.Config;
import com.nightmarecore.ModTags;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.entity.NightmareEntity;
import com.nightmarecore.registry.ModItems;
import com.nightmarecore.util.CurioUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * All fragment generation lives here. The chance deliberately does not read
 * Looting or Fortune; only the vanilla Luck attribute is used.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FragmentDropHandler {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.isCanceled() || !Config.ENABLE_FRAGMENT_DROPS.get()
                || !Config.DROP_FROM_MOBS.get()) {
            return;
        }

        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || victim.isRemoved()) {
            return;
        }
        if (victim instanceof NightmareEntity
                || victim.getPersistentData().getBoolean(NightmareEventHandler.NIGHTMARE_TAG)) {
            // The Nightmare has its own guaranteed drop table.
            return;
        }

        Entity killerEntity = event.getSource().getEntity();
        if (!(killerEntity instanceof ServerPlayer player)
                || player instanceof FakePlayer
                || player.isCreative()
                || player.isSpectator()
                || player.isRemoved()) {
            return;
        }

        if (victim == player || victim instanceof ArmorStand) {
            return;
        }
        if (victim instanceof Player && !Config.ALLOW_PLAYER_KILLS.get()) {
            return;
        }
        if (!Config.ALLOW_PASSIVE_MOBS.get() && !(victim instanceof Enemy)) {
            return;
        }
        if (victim.getType().is(ModTags.NO_FRAGMENT_DROP_ENTITIES)) {
            return;
        }
        if (!CurioUtil.hasNightmareCore(player) || !roll(player)) {
            return;
        }

        int count = fragmentCount(player.getRandom());
        for (int i = 0; i < count; i++) {
            event.getDrops().add(createFragment(level, victim.getX(), victim.getY(), victim.getZ(),
                    player.getRandom()));
        }

        // No Looting/EnchantmentHelper call here on purpose.
        NightmareCore.LOGGER.debug("Nightmare fragments: {} dropped after {} killed by {}",
                count, BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()), player.getGameProfile().getName());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !Config.ENABLE_FRAGMENT_DROPS.get()
                || !Config.DROP_FROM_BLOCKS.get()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Player eventPlayer = event.getPlayer();
        if (!(eventPlayer instanceof ServerPlayer player)
                || player instanceof FakePlayer
                || player.isCreative()
                || player.isSpectator()
                || player.isRemoved()) {
            return;
        }

        if (event.getState().is(ModTags.NO_FRAGMENT_DROP_BLOCKS)) {
            return;
        }
        if (!CurioUtil.hasNightmareCore(player) || !roll(player)) {
            return;
        }

        int count = fragmentCount(player.getRandom());
        BlockPos pos = event.getPos();
        for (int i = 0; i < count; i++) {
            level.addFreshEntity(createFragment(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    player.getRandom()));
        }

        NightmareCore.LOGGER.debug("Nightmare fragments: {} dropped after {} broke {}",
                count, player.getGameProfile().getName(),
                BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()));
    }

    private static boolean roll(ServerPlayer player) {
        double luckLevel = Math.floor(player.getAttributeValue(Attributes.LUCK));
        double charmBonus = CurioUtil.hasBlessing(player, ModItems.DREAMHUNTER_EYE.get())
                ? Config.CHARM_BONUS_PERCENT.get()
                : 0.0D;
        double chance = Config.BASE_CHANCE_PERCENT.get()
                + Config.CHANCE_PER_LUCK_LEVEL.get() * luckLevel
                + charmBonus;
        chance = Mth.clamp(chance, 0.0D, 100.0D);

        if (chance <= 0.0D) {
            return false;
        }
        return chance >= 100.0D || player.getRandom().nextDouble() * 100.0D < chance;
    }

    private static int fragmentCount(RandomSource random) {
        int min = Math.min(Config.MIN_FRAGMENTS.get(), Config.MAX_FRAGMENTS.get());
        int max = Math.max(Config.MIN_FRAGMENTS.get(), Config.MAX_FRAGMENTS.get());
        return min + random.nextInt(max - min + 1);
    }

    private static ItemEntity createFragment(ServerLevel level, double x, double y, double z,
                                             RandomSource random) {
        ItemEntity itemEntity = new ItemEntity(level, x, y, z,
                new ItemStack(ModItems.NIGHTMARE_FRAGMENT.get()));
        itemEntity.setPickUpDelay(10);
        itemEntity.setDeltaMovement(
                (random.nextDouble() - 0.5D) * 0.18D,
                0.18D + random.nextDouble() * 0.12D,
                (random.nextDouble() - 0.5D) * 0.18D);
        return itemEntity;
    }

    private FragmentDropHandler() {
    }
}
