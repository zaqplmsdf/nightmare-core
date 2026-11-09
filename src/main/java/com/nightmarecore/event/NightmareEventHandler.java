package com.nightmarecore.event;

import com.nightmarecore.Config;
import com.nightmarecore.CurseType;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.entity.NightmareEntity;
import com.nightmarecore.registry.ModEntities;
import com.nightmarecore.registry.ModItems;
import com.nightmarecore.util.CurioUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Sleep encounter for all five Nightmare types, plus their guaranteed drops.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NightmareEventHandler {

    public static final String NIGHTMARE_TAG = NightmareEntity.NIGHTMARE_TAG;

    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)
                || player.level().isClientSide
                || !CurioUtil.hasNightmareCore(player)) {
            return;
        }

        // Entering a bed always resets the seven-day insomnia timer and clears insanity.
        CurseEventHandler.markSlept(player);

        if (level.getDifficulty() == Difficulty.PEACEFUL
                || CurseType.SLEEP_NIGHTMARE.isSuppressed(player)
                || player.getRandom().nextDouble() * 100.0D >= Config.NIGHTMARE_SPAWN_CHANCE_PERCENT.get()) {
            return;
        }

        BlockPos bed = event.getOptionalPos().orElse(player.blockPosition());
        if (!spawnNightmare(level, player, bed)) {
            return;
        }

        level.playSound(null, bed, SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.0F, 0.6F);
        level.sendParticles(ParticleTypes.SOUL,
                bed.getX() + 0.5D, bed.getY() + 0.7D, bed.getZ() + 0.5D,
                30, 1.2D, 0.7D, 1.2D, 0.02D);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.nightmare_core.nightmare.spawned")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
                    true);
        }
    }

    /**
     * Package-visible for the development mechanics test.
     */
    public static boolean spawnNightmare(ServerLevel level, Player target, BlockPos bed) {
        return NightmareGroupSpawner.spawnGroup(level, target, bed,
                NightmareGroupSpawner.MAX_GROUP, true) > 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onNightmareDrops(LivingDropsEvent event) {
        if (event.isCanceled()
                || (!(event.getEntity() instanceof NightmareEntity)
                        && !event.getEntity().getPersistentData().getBoolean(NIGHTMARE_TAG))
                || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        // The Nightmare is a custom encounter, not a normal zombie loot source.
        event.getDrops().clear();
        int fragments = 1 + level.random.nextInt(2);
        for (int i = 0; i < fragments; i++) {
            event.getDrops().add(drop(level, event.getEntity().getX(), event.getEntity().getY(),
                    event.getEntity().getZ(), new ItemStack(ModItems.NIGHTMARE_FRAGMENT.get())));
        }
        int materials = 1 + level.random.nextInt(2);
        for (int i = 0; i < materials; i++) {
            event.getDrops().add(drop(level, event.getEntity().getX(), event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModEntities.materialFor(event.getEntity().getType()))));
        }
        if (level.random.nextFloat() < 0.05F) {
            event.getDrops().add(drop(level, event.getEntity().getX(), event.getEntity().getY(),
                    event.getEntity().getZ(),
                    new ItemStack(ModEntities.blessingFor(event.getEntity().getType()))));
        }
        if (level.random.nextDouble() * 100.0D < Config.BLESSING_BAG_CHANCE_PERCENT.get()) {
            event.getDrops().add(drop(level, event.getEntity().getX(), event.getEntity().getY(),
                    event.getEntity().getZ(), new ItemStack(ModItems.BLESSING_BAG.get())));
        }
    }

    private static ItemEntity drop(ServerLevel level, double x, double y, double z, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, x, y, z, stack);
        item.setDefaultPickUpDelay();
        item.setDeltaMovement(
                (level.random.nextDouble() - 0.5D) * 0.15D,
                0.2D + level.random.nextDouble() * 0.1D,
                (level.random.nextDouble() - 0.5D) * 0.15D);
        return item;
    }

    private NightmareEventHandler() {
    }
}
