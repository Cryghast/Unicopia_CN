package com.minelittlepony.unicopia.magic;

import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.entity.projectile.ProjectileEntity;

/**
 * Interface for a magic spells
 */
public interface MagicEffect extends NbtSerialisable, Affine {

    /**
     * Gets the name used to identify this effect.
     */
    String getName();

    /**
     * Gets the tint for this spell when applied to a gem.
     */
    int getTint();

    /**
     * Sets this effect as dead.
     */
    void setDead();

    /**
     * Returns true if this spell is dead, and must be cleaned up.
     */
    boolean isDead();

    /**
     * Returns true if this effect has changes that need to be sent to the client.
     */
    boolean isDirty();

    /**
     * Marks this effect as dirty.
     */
    void setDirty(boolean dirty);

    /**
     * Returns true if this effect can be crafted into a gem.
     */
    boolean isCraftable();

    /**
     * Gets the highest level this spell can be safely operated at.
     * Gems may go higher, however chance of explosion/exhaustion increases with every level.
     */
    int getMaxLevelCutOff(Caster<?> caster);

    float getMaxExhaustion(Caster<?> caster);

    /**
     * Gets the chances of this effect turning into an innert gem or exploding.
     */
    float getExhaustion(Caster<?> caster);

    /**
     * Called when first attached to a gem.
     */
    default void onPlaced(Caster<?> caster) {

    }

    /**
     * Called when a gem is destroyed.
     */
    default void onDestroyed(Caster<?> caster) {
        setDead();
    }

    default boolean handleProjectileImpact(ProjectileEntity projectile) {
        return false;
    }

    /**
     * Called every tick when attached to an entity.
     * Called on both sides.
     *
     * @param source   The entity we are currently attached to.
     */
    boolean update(Caster<?> source);

    /**
     * Called every tick when attached to an entity to produce particle effects.
     * Is only called on the client side.
     *
     * @param source    The entity we are attached to.
     */
    void render(Caster<?> source);

    /**
     * Return true to allow the gem update and move.
     */
    default boolean allowAI() {
        return false;
    }

    /**
     * Returns a new, deep-copied instance of this spell.
     */
    default MagicEffect copy() {
        return SpellRegistry.instance().copyInstance(this);
    }
}
