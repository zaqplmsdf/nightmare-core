package com.nightmarecore.entity;

import com.nightmarecore.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Slow, gravity-free Nightmare Eye projectile. Its velocity is kept constant
 * and only expires on a hit or after a lifetime.
 */
public class NightmareProjectile extends Projectile {

    private float damage = 3.0F;

    public NightmareProjectile(EntityType<NightmareProjectile> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public NightmareProjectile(Level level, LivingEntity owner, float damage) {
        this(ModEntities.NIGHTMARE_PROJECTILE.get(), level);
        this.setOwner(owner);
        this.damage = damage;
        this.setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        Vec3 movement = this.getDeltaMovement();
        super.tick();
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            this.onHit(hit);
        }
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        this.setDeltaMovement(movement);
        if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        if (target.level() instanceof ServerLevel) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), this.damage);
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("Damage");
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
