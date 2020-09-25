package com.minelittlepony.unicopia.projectile;

import com.minelittlepony.unicopia.ability.magic.ThrowableSpell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface Projectile {

    void setGravity(boolean gravity);

    void setItem(ItemStack stack);

    void setOwner(LivingEntity owner);

    void setEffect(ThrowableSpell effect);

    void setThrowDamage(float damage);

    float getThrowDamage();

    void setHydrophobic();

    boolean getHydrophobic();

    void launch(Entity entityThrower, float pitch, float yaw, float offset, float velocity, float inaccuracy);

    default void spawn(World world) {
        world.spawnEntity((Entity)this);
    }

    default void launch(double x, double y, double z, float velocity, float inaccuracy) {
        ((ProjectileEntity)this).setVelocity(x, y, z, velocity, inaccuracy);
    }
}