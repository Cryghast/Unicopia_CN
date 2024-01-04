package com.minelittlepony.unicopia.ability;

import java.util.Optional;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarrotsBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Earth Pony ability to grow crops
 */
public class EarthPonyGrowAbility implements Ability<Pos> {

    @Override
    public int getWarmupTime(Pony player) {
        return 10;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 50;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canUseEarth();
    }

    @Override
    public Optional<Pos> prepare(Pony player) {
        return TraceHelper.findBlock(player.asEntity(), 3, 1).map(Pos::new);
    }

    @Override
    public Hit.Serializer<Pos> getSerializer() {
        return Pos.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 10;
    }

    @Override
    public boolean apply(Pony player, Pos data) {
        int count = 0;

        for (BlockPos pos : BlockPos.iterate(
                data.pos().add(-2, -2, -2),
                data.pos().add( 2,  2,  2))) {
            count += applySingle(player, player.asWorld(), player.asWorld().getBlockState(pos), pos);
        }

        if (count > 0) {
            player.subtractEnergyCost(count / 5D);
        }
        return true;
    }

    protected int applySingle(Pony player, World w, BlockState state, BlockPos pos) {

        ItemStack stack = new ItemStack(Items.BONE_MEAL);

        if (state.getBlock() instanceof Growable growable) {
            return growable.grow(w, state, pos) ? 1 : 0;
        }

        if (state.isOf(Blocks.CARROTS)) {
            if (state.get(CarrotsBlock.AGE) == CarrotsBlock.MAX_AGE) {
                boolean transform = w.random.nextInt(3) == 0;
                DoubleSupplier vecComponentFactory = () -> w.random.nextTriangular(0, 0.5);
                Supplier<Vec3d> posSupplier = () -> pos.toCenterPos().add(VecHelper.supply(vecComponentFactory));

                for (int i = 0; i < 25; i++) {
                    ParticleUtils.spawnParticle(w, new MagicParticleEffect(0xFFFF00), posSupplier.get(), Vec3d.ZERO);
                    if (transform) {
                        ParticleUtils.spawnParticle(w, ParticleTypes.CLOUD, posSupplier.get(), Vec3d.ZERO);
                    }
                }

                if (transform) {
                    w.setBlockState(pos, UBlocks.GOLD_ROOT.getDefaultState().with(CarrotsBlock.AGE, CarrotsBlock.MAX_AGE));
                }

                return 5;
            }
        }

        if (w.getBlockState(pos).isIn(UTags.UNAFFECTED_BY_GROW_ABILITY)) {
            return 0;
        }

        if (BoneMealItem.useOnFertilizable(stack, w, pos)) {
            if (w.random.nextInt(350) == 0) {
                if (w.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) {
                    FarmlandBlock.setToDirt(null, state, w, pos.down());
                }
                w.setBlockState(pos, UBlocks.PLUNDER_VINE_BUD.getDefaultState());
            } else if (w.random.nextInt(5000) == 0) {
                if (w.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) {
                    FarmlandBlock.setToDirt(null, state, w, pos.down());
                }
                UBlocks.CURING_JOKE.grow(w, state, pos);
            }
            return 1;
        }

        if (BoneMealItem.useOnGround(stack, w, pos, Direction.UP)) {
            return 1;
        }

        return 0;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(30);

        if (player.asWorld().isClient()) {
            player.spawnParticles(MagicParticleEffect.UNICORN, 1);
        }
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {

    }

    public interface Growable {
        boolean grow(World world, BlockState state, BlockPos pos);
    }
}
