package com.nightmarecore.entity;

import com.nightmarecore.Config;
import com.nightmarecore.registry.ModEntities;
import com.nightmarecore.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Shared implementation for the five Nightmare variants. All stats are loaded
 * from the nightmare config; defaults intentionally match a vanilla zombie.
 */
public class NightmareEntity extends Zombie implements GeoEntity {

    public static final String NIGHTMARE_TAG = "nightmare_core:nightmare";
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public NightmareEntity(EntityType<NightmareEntity> type, Level level) {
        super(type, level);

        this.getPersistentData().putBoolean(NIGHTMARE_TAG, true);
        this.setPersistenceRequired();
        this.setBaby(false);
        this.setCanBreakDoors(false);
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

        var health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            double healthValue = stats().health().get();
            health.setBaseValue(healthValue);
            this.setHealth((float) healthValue);
        }
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(stats().movementSpeed().get());
        }
        var follow = this.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow != null) {
            follow.setBaseValue(stats().followRange().get());
        }
        var armor = this.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.setBaseValue(stats().armor().get());
        }
        var damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(damageFor(level.getDifficulty()));
        }
    }

    public Config.VariantStats stats() {
        if (isEye()) {
            return Config.EYE;
        }
        if (isSkull()) {
            return Config.SKULL;
        }
        if (isHeart()) {
            return Config.HEART;
        }
        if (isShadow()) {
            return Config.SHADOW;
        }
        return Config.HAND;
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

    public float damageFor(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> (float) (double) stats().attackEasy().get();
            case NORMAL -> (float) (double) stats().attackNormal().get();
            case HARD -> (float) (double) stats().attackHard().get();
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
            float percent = (float) (double) stats().sunlightPercent().get() / 100.0F;
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

        int rangedCooldown = Math.max(5, Config.EYE_RANGED_COOLDOWN_TICKS.get());
        if (isEye() && this.tickCount % rangedCooldown == 0) {
            LivingEntity target = this.getTarget();
            double minDistance = Config.EYE_RANGED_DISTANCE.get();
            if (target != null && target.isAlive()
                    && this.distanceToSqr(target) > minDistance * minDistance
                    && this.distanceToSqr(target) < 400.0D
                    && this.hasLineOfSight(target)) {
                this.rangedAttack(target);
            }
        }
    }

    private void rangedAttack(LivingEntity target) {
        this.triggerAnim("attack", "glare");
        NightmareProjectile projectile = new NightmareProjectile(
                this.level(), this, (float) (double) Config.EYE_PROJECTILE_DAMAGE.get());
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - projectile.getY();
        double dz = target.getZ() - this.getZ();
        projectile.shoot(dx, dy, dz, (float) (double) Config.EYE_PROJECTILE_SPEED.get(), 0.0F);
        this.level().addFreshEntity(projectile);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.NIGHTMARE_EYE_GLARE.get(), SoundSource.HOSTILE, 0.9F, 1.0F);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && isEye()) {
            this.triggerAnim("attack", "bite");
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.NIGHTMARE_EYE_BITE.get(), SoundSource.HOSTILE, 0.9F, 1.0F);
        }
        if (hit && isHand() && target instanceof LivingEntity living) {
            var effect = this.getRandom().nextBoolean() ? MobEffects.POISON : MobEffects.WITHER;
            living.addEffect(new MobEffectInstance(effect, Config.HAND_EFFECT_DURATION_TICKS.get(), 0));
        }
        return hit;
    }

    private boolean isInSunlight() {
        return this.level().isDay()
                && this.level().canSeeSky(this.blockPosition())
                && !this.level().isClientSide;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof AbstractVillager) {
            target = null;
        }
        super.setTarget(target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isEye() ? ModSounds.NIGHTMARE_EYE_IDLE.get() : super.getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return isEye() ? ModSounds.NIGHTMARE_EYE_HURT.get() : super.getHurtSound(source);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("move"))));
        controllers.add(new AnimationController<>(this, "attack", 0,
                state -> PlayState.STOP)
                .triggerableAnim("glare", RawAnimation.begin().thenPlay("glare"))
                .triggerableAnim("bite", RawAnimation.begin().thenPlay("bite")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
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
