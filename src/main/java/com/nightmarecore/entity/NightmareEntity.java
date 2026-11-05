package com.nightmarecore.entity;

import com.nightmarecore.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Shared implementation for the five Nightmare variants. All stats are loaded
 * from the nightmare config; defaults intentionally match a vanilla zombie.
 */
public class NightmareEntity extends Zombie {

    public static final String NIGHTMARE_TAG = "nightmare_core:nightmare";

    public NightmareEntity(EntityType<NightmareEntity> type, Level level) {
        super(type, level);

        this.getPersistentData().putBoolean(NIGHTMARE_TAG, true);
        this.setPersistenceRequired();
        this.setBaby(false);
        this.setCanBreakDoors(false);
        this.setCustomName(Component.translatable(type.getDescriptionId()));
        this.setCustomNameVisible(true);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

        var health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(Config.NIGHTMARE_HEALTH.get());
            this.setHealth((float) (double) Config.NIGHTMARE_HEALTH.get());
        }
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(Config.NIGHTMARE_SPEED.get());
        }
        var follow = this.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null) {
            follow.setBaseValue(Config.NIGHTMARE_FOLLOW_RANGE.get());
        }
        var armor = this.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.setBaseValue(Config.NIGHTMARE_ARMOR.get());
        }
        var damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(damageFor(level.getDifficulty()));
        }
    }

    public static float damageFor(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> (float) (double) Config.NIGHTMARE_ATTACK_EASY.get();
            case NORMAL -> (float) (double) Config.NIGHTMARE_ATTACK_NORMAL.get();
            case HARD -> (float) (double) Config.NIGHTMARE_ATTACK_HARD.get();
            default -> 0.0F;
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        if (this.tickCount % 20 == 0 && isInSunlight()) {
            float percent = (float) (double) Config.NIGHTMARE_SUNLIGHT_DAMAGE_PERCENT.get() / 100.0F;
            float damage = this.getMaxHealth() * percent;
            if (damage > 0.0F) {
                this.invulnerableTime = 0;
                this.hurt(this.damageSources().magic(), damage);
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                        10, 0.35D, 0.5D, 0.35D, 0.02D);
            }
        } else if (this.tickCount % 40 == 0) {
            serverLevel.sendParticles(ParticleTypes.SOUL,
                    this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                    1, 0.25D, 0.35D, 0.25D, 0.0D);
        }
    }

    private boolean isInSunlight() {
        return this.level().isDay()
                && this.level().canSeeSky(this.blockPosition())
                && !this.level().isClientSide;
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    public int getExperienceReward() {
        return 5;
    }
}
