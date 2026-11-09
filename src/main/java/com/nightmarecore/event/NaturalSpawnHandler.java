package com.nightmarecore.event;

import com.nightmarecore.Config;
import com.nightmarecore.NightmareCore;
import com.nightmarecore.entity.NightmareEntity;
import com.nightmarecore.util.CurioUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Natural Nightmare spawning around players who wear the core. The selected
 * variant is weighted by the current moon phase in ModEntities.createRandom.
 */
@Mod.EventBusSubscriber(modid = NightmareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NaturalSpawnHandler {

    private static final int MAX_ATTEMPTS = 16;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.isSpectator()) {
            return;
        }
        int interval = Math.max(20, Config.NATURAL_SPAWN_INTERVAL_TICKS.get());
        if (player.tickCount % interval == 0) {
            trySpawn(player);
        }
    }

    public static boolean trySpawn(ServerPlayer player) {
        if (!Config.NATURAL_SPAWN_ENABLED.get() || !CurioUtil.hasNightmareCore(player)) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        if (level.isDay() || level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return false;
        }
        if (player.getRandom().nextFloat() * 100.0F >= Config.NATURAL_SPAWN_CHANCE_PERCENT.get()) {
            return false;
        }

        double minDistance = Config.NATURAL_SPAWN_MIN_DISTANCE.get();
        double maxDistance = Math.max(minDistance + 1.0D, Config.NATURAL_SPAWN_MAX_DISTANCE.get());
        int maxNearby = Config.NATURAL_SPAWN_MAX_NEARBY.get();
        int nearby = level.getEntitiesOfClass(NightmareEntity.class,
                player.getBoundingBox().inflate(maxDistance),
                NightmareEntity::isAlive).size();
        if (nearby >= maxNearby) {
            return false;
        }

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            double distance = minDistance + player.getRandom().nextDouble() * (maxDistance - minDistance);
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, player.getBlockY(), z));
            if (Math.abs(top.getY() - player.getY()) > 24.0D) {
                continue;
            }
            BlockPos below = top.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                    || !level.getBlockState(top).getFluidState().isEmpty()
                    || !level.getBlockState(top.above()).getFluidState().isEmpty()
                    || !level.isEmptyBlock(top)
                    || !level.isEmptyBlock(top.above())) {
                continue;
            }

            int remaining = maxNearby - nearby;
            int spawned = NightmareGroupSpawner.spawnGroup(level, player, top, remaining, false);
            return spawned > 0;
        }
        return false;
    }

    private NaturalSpawnHandler() {
    }
}
