package com.nightmarecore.event;

import com.nightmarecore.entity.NightmareEntity;
import com.nightmarecore.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Spawns a group of 1-6 random Nightmares around a center position. Used by
 * both the natural spawner and the Curse 1 (sleep encounter) spawner.
 */
public final class NightmareGroupSpawner {

    public static final int MIN_GROUP = 1;
    public static final int MAX_GROUP = 6;
    private static final int MAX_ATTEMPTS_PER_MOB = 32;

    public static int spawnGroup(ServerLevel level, Player target, BlockPos center,
                                 int maxCount, boolean fireResistance) {
        if (maxCount <= 0) {
            return 0;
        }

        int count = MIN_GROUP + level.random.nextInt(MAX_GROUP - MIN_GROUP + 1);
        count = Math.min(count, maxCount);

        int spawned = 0;
        for (int i = 0; i < count; i++) {
            for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_MOB; attempt++) {
                int dx = level.random.nextInt(13) - 6;
                int dz = level.random.nextInt(13) - 6;
                if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                    continue;
                }
                int dy = level.random.nextInt(7) - 3;
                BlockPos pos = center.offset(dx, dy, dz);
                BlockState below = level.getBlockState(pos.below());
                if (!level.isEmptyBlock(pos)
                        || !level.isEmptyBlock(pos.above())
                        || !below.isFaceSturdy(level, pos.below(), Direction.UP)) {
                    continue;
                }

                NightmareEntity nightmare = ModEntities.createRandom(level, level.random);
                if (nightmare == null) {
                    return spawned;
                }
                nightmare.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                        level.random.nextFloat() * 360.0F, 0.0F);
                if (!level.noCollision(nightmare)) {
                    continue;
                }

                nightmare.getPersistentData().putBoolean(NightmareEntity.NIGHTMARE_TAG, true);
                if (fireResistance) {
                    nightmare.addEffect(new MobEffectInstance(
                            MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
                }
                nightmare.setTarget(target);
                level.addFreshEntity(nightmare);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        18, 0.4D, 0.6D, 0.4D, 0.03D);
                spawned++;
                break;
            }
        }
        return spawned;
    }

    private NightmareGroupSpawner() {
    }
}
