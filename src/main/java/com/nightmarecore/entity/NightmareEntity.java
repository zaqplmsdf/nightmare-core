package com.nightmarecore.entity;

import com.nightmarecore.Config;
import com.nightmarecore.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
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
            double healthValue = Config.NIGHTMARE_HEALTH.get();
            if (isSkull()) {
                healthValue *= 1.6D;
            }
            health.setBaseValue(healthValue);
            this.setHealth((float) healthValue);
        }
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            double speedValue = Config.NIGHTMARE_SPEED.get();
            if (isShadow()) {
                speedValue *= 1.5D;
            }
            if (isHeart()) {
                speedValue = 0.0D;
            }
            speed.setBaseValue(speedValue);
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
            double damageValue = damageFor(level.getDifficulty());
            if (isSkull()) {
                damageValue *= 0.7D;
            }
            damage.setBaseValue(damageValue);
        }
    }

    private boolean isEye() {
        return this.getType() == ModEntities.RIFT_NIGHTMARE.get();
    }

    private boolean isSkull() {
        return this.getType() == ModEntities.ASHEN_NIGHTMARE.get();
    }

    private boolean isHeart() {
        return this.getType() == ModEntities.TIDAL_NIGHTMARE.get();
    }

    private boolean isShadow() {
        return this.getType() == ModEntities.VIGIL_NIGHTMARE.get();
    }

    private boolean isHand() {
        return this.getType() == ModEntities.HOLLOW_NIGHTMARE.get();
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

        if (isEye() && this.tickCount % 30 == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()
                    && this.distanceToSqr(target) > 16.0D
                    && this.distanceToSqr(target) < 400.0D
                    && this.hasLineOfSight(target)) {
                this.rangedAttack(target);
            }
        }
    }

    private void rangedAttack(LivingEntity target) {
        Arrow arrow = new Arrow(this.level(), this);
        arrow.setOwner(this);
        arrow.setBaseDamage(damageFor(this.level().getDifficulty()));
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - arrow.getY();
        double dz = target.getZ() - this.getZ();
        arrow.shoot(dx, dy, dz, 1.6F, 0.05F);
        this.level().addFreshEntity(arrow);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.ARROW_SHOOT,
                net.minecraft.sounds.SoundSource.HOSTILE, 0.8F, 1.1F);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && isHand() && target instanceof LivingEntity living) {
            var effect = this.getRandom().nextBoolean() ? MobEffects.POISON : MobEffects.WITHER;
            living.addEffect(new MobEffectInstance(effect, 100, 0));
        }
        return hit;
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
