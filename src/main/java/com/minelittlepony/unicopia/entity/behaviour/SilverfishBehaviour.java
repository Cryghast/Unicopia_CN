package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

public class SilverfishBehaviour extends EntityBehaviour<SilverfishEntity> {
    @Override
    public void update(Pony player, SilverfishEntity entity, Disguise spell) {
        if (!player.isClient() && player.sneakingChanged() && player.asEntity().isSneaking()) {
            BlockPos pos = entity.getBlockPos().down();
            BlockState state = entity.getWorld().getBlockState(pos);

            if (StateMaps.SILVERFISH_AFFECTED.convert(entity.getWorld(), pos)) {
                entity.getWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
            }
        }
    }
}
