package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.SpellAttributes;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AreaProtectionSpell extends AbstractAreaEffectSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 50)
            .with(Trait.STRENGTH, 30)
            .build();


    static void appendTooltip(CustomisedSpellType<AreaProtectionSpell> type, List<Text> tooltip) {
        tooltip.add(SpellAttributes.CAST_ON_LOCATION);
        tooltip.add(SpellAttributes.of(SpellAttributes.RANGE, 4 + type.traits().get(Trait.POWER)));
    }

    protected AreaProtectionSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    /*@Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return method == CastingMethod.STAFF || getTraits().get(Trait.GENEROSITY) > 0 ? toPlaceable() : this;
    }*/

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation == Situation.PROJECTILE || situation == Situation.BODY) {
            return false;
        }

        float radius = (float)getRange(source);

        if (source.isClient()) {
            Vec3d origin = source.getOriginVector();

            source.spawnParticles(origin, new Sphere(true, radius), (int)(radius * 6), pos -> {
                if (!source.asWorld().isAir(BlockPos.ofFloored(pos))) {
                    source.addParticle(new MagicParticleEffect(getType().getColor()), pos, Vec3d.ZERO);
                }
            });
        } else {
            Ether.get(source.asWorld()).getOrCreate(this, source).setRadius(radius);
        }

        source.findAllSpellsInRange(radius, e -> isValidTarget(source, e)).filter(caster -> !caster.hasCommonOwner(source)).forEach(caster -> {
            caster.asEntity().kill();
        });

        return !isDead();
    }

    private double getRange(Caster<?> source) {
        float multiplier = source instanceof Pony pony && pony.asEntity().isSneaking() ? 1 : 2;
        float min = 4 + getAdditionalRange();
        double range = (min + (source.getLevel().getScaled(4) * 2)) / multiplier;
        if (source instanceof Pony && range > 2) {
            range = Math.sqrt(range);
        }
        return range;
    }

    public boolean blocksMagicFor(Caster<?> source, Caster<?> other, Vec3d position) {
        return !FriendshipBraceletItem.isComrade(other, other.asEntity())
                && source.getOriginVector().distanceTo(position) <= getRange(source);
    }

    protected boolean isValidTarget(Caster<?> source, Entity entity) {
        return entity.getType() == UEntities.MAGIC_BEAM;
    }
}
