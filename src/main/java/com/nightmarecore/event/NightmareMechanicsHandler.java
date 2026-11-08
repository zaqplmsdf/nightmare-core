package com.nightmarecore.event;

import com.nightmarecore.NightmareCore;
import com.nightmarecore.entity.NightmareEntity;
import com.nightmarecore.registry.ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Variant mechanics for the five Nightmares.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NightmareMechanicsHandler {

    private static final double HEART_AURA_RADIUS = 16.0D;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof NightmareEntity victim)) {
            return;
        }

        // Heart: nearby Nightmares are protected until the Heart is destroyed.
        if (victim.getType() != ModEntities.TIDAL_NIGHTMARE.get()
                && hasHeartNearby(victim)) {
            event.setCanceled(true);
            return;
        }

        // Shadow: 40% reduction against unenchanted weapons.
        if (victim.getType() == ModEntities.VIGIL_NIGHTMARE.get()) {
            Entity source = event.getSource().getEntity();
            if (source instanceof Player player) {
                ItemStack weapon = player.getMainHandItem();
                if (!weapon.isEmpty() && !weapon.isEnchanted()) {
                    event.setAmount(event.getAmount() * 0.6F);
                }
            }
        }
    }

    private static boolean hasHeartNearby(NightmareEntity victim) {
        return victim.level().getEntitiesOfClass(NightmareEntity.class,
                        victim.getBoundingBox().inflate(HEART_AURA_RADIUS),
                        entity -> entity.getType() == ModEntities.TIDAL_NIGHTMARE.get()
                                && entity.isAlive())
                .stream()
                .anyMatch(LivingEntity::isAlive);
    }

    private NightmareMechanicsHandler() {
    }
}
