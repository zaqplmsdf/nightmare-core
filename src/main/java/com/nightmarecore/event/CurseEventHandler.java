package com.nightmarecore.event;

import com.nightmarecore.CurseType;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.registry.ModEffects;
import com.nightmarecore.util.CurioUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Implements the seven core curses and their blessing-based suppression.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CurseEventHandler {

    public static final String LAST_SLEEP_TAG = "nightmare_core:last_sleep";

    private static final int DARK_DELAY_TICKS = 100;
    private static final int DARK_INTERVAL_TICKS = 20;
    private static final long INSANITY_TICKS = 7L * 24000L;
    private static final double HOSTILE_RADIUS = 16.0D;

    private static final Map<UUID, Integer> DARK_TICKS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player instanceof FakePlayer
                || player.isSpectator()
                || !player.isAlive()) {
            return;
        }

        UUID uuid = player.getUUID();
        if (!CurioUtil.hasNightmareCore(player)) {
            DARK_TICKS.remove(uuid);
            player.removeEffect(ModEffects.INSANITY.get());
            return;
        }

        tickHealthCap(player);
        tickDarkness(player, uuid);
        tickInsanity(player);
    }

    static void tickHealthCap(ServerPlayer player) {
        if (CurseType.HEALTH_CAP.isSuppressed(player)) {
            return;
        }
        float cap = healthCap(player);
        if (player.getHealth() > cap) {
            player.setHealth(cap);
        }
    }

    static void tickDarkness(ServerPlayer player, UUID uuid) {
        if (CurseType.DARKNESS.isSuppressed(player) || !isDark(player)) {
            DARK_TICKS.remove(uuid);
            return;
        }

        int ticks = DARK_TICKS.merge(uuid, 1, Integer::sum);
        if (ticks >= DARK_DELAY_TICKS && (ticks - DARK_DELAY_TICKS) % DARK_INTERVAL_TICKS == 0) {
            damageFromDarkness(player);
        }
    }

    private static boolean isDark(ServerPlayer player) {
        return player.serverLevel().getMaxLocalRawBrightness(player.blockPosition()) < 3;
    }

    private static void damageFromDarkness(ServerPlayer player) {
        float damage = (float) Math.ceil(player.getMaxHealth() * 0.20D);
        if (player.getHealth() <= 1.0F) {
            return;
        }
        player.setHealth(Math.max(1.0F, player.getHealth() - damage));
    }

    static void tickInsanity(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        long now = player.level().getGameTime();
        if (!data.contains(LAST_SLEEP_TAG)) {
            data.putLong(LAST_SLEEP_TAG, now);
            return;
        }

        long lastSleep = data.getLong(LAST_SLEEP_TAG);
        boolean overdue = now - lastSleep >= INSANITY_TICKS;
        if (overdue && !CurseType.INSANITY.isSuppressed(player)) {
            if (!player.hasEffect(ModEffects.INSANITY.get())) {
                player.addEffect(new MobEffectInstance(
                        ModEffects.INSANITY.get(), Integer.MAX_VALUE, 0, false, true, true));
                player.displayClientMessage(
                        Component.translatable("message.nightmare_core.insanity")
                                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
                        false);
            }
        } else {
            player.removeEffect(ModEffects.INSANITY.get());
        }
    }

    /**
     * Called when the player enters a bed; resets the insomnia timer and clears
     * the insanity effect even if the sleep is interrupted later.
     */
    public static void markSlept(Player player) {
        player.getPersistentData().putLong(LAST_SLEEP_TAG, player.level().getGameTime());
        player.removeEffect(ModEffects.INSANITY.get());
    }

    public static float healthCap(Player player) {
        int steps = Math.min(8, Math.max(0, player.experienceLevel) / 20);
        double fraction = 0.1D + 0.1D * steps;
        return (float) (player.getMaxHealth() * fraction);
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !CurioUtil.hasNightmareCore(player)
                || CurseType.HEALTH_CAP.isSuppressed(player)) {
            return;
        }
        float cap = healthCap(player);
        float allowed = Math.max(0.0F, cap - player.getHealth());
        if (event.getAmount() > allowed) {
            event.setAmount(allowed);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }

        LivingEntity victim = event.getEntity();

        // Curse 5: damage taken while awake is doubled.
        if (victim instanceof ServerPlayer player
                && CurseType.active(player, CurseType.AWAKE_DOUBLE_DAMAGE)
                && !player.isSleeping()) {
            event.setAmount(event.getAmount() * 2.0F);
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || victim == attacker
                || !CurioUtil.hasNightmareCore(attacker)) {
            return;
        }

        // Curse 3: outgoing damage <= 5 is nullified; otherwise reduced by 20%.
        if (!CurseType.DAMAGE_PENALTY.isSuppressed(attacker)) {
            if (event.getAmount() <= 5.0F) {
                event.setCanceled(true);
                return;
            }
            event.setAmount(event.getAmount() * 0.8F);
        }

        // Curse 7: insanity can turn an attack against its owner.
        if (attacker.hasEffect(ModEffects.INSANITY.get())
                && event.getAmount() > 0.0F
                && attacker.getRandom().nextFloat() < 0.2F) {
            attacker.hurt(attacker.damageSources().magic(), event.getAmount() * 0.5F);
            attacker.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                    attacker.getX(), attacker.getY() + 1.0D, attacker.getZ(),
                    8, 0.3D, 0.4D, 0.3D, 0.02D);
        }
    }

    @SubscribeEvent
    public static void onSetSpawn(PlayerSetSpawnEvent event) {
        if (event.isForced()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !CurseType.active(player, CurseType.SPAWN_POINT)) {
            return;
        }

        BlockPos spawn = event.getNewSpawn();
        if (spawn == null) {
            return;
        }
        ServerLevel level = player.server.getLevel(event.getSpawnLevel());
        if (level == null) {
            level = player.serverLevel();
        }
        if (!hasHostileNearby(level, spawn)) {
            event.setCanceled(true);
            player.displayClientMessage(
                    Component.translatable("message.nightmare_core.spawn_point.no_hostiles")
                            .withStyle(ChatFormatting.RED),
                    true);
        }
    }

    private static boolean hasHostileNearby(ServerLevel level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(HOSTILE_RADIUS, 8.0D, HOSTILE_RADIUS);
        return level.getEntitiesOfClass(LivingEntity.class, area,
                        entity -> entity instanceof Enemy && entity.isAlive())
                .stream()
                .anyMatch(LivingEntity::isAlive);
    }

    private CurseEventHandler() {
    }
}

