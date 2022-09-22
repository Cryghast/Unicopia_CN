package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.passive.*;

public class SteedBehaviour<T extends HorseBaseEntity> extends EntityBehaviour<T> {

    @Override
    public void update(Pony player, T horse, Disguise spell) {
        boolean angry = !player.getEntity().isOnGround() && player.getMaster().isSprinting();
        boolean sneaking = isSneakingOnGround(player);

        angry |= sneaking;
        if (player.sneakingChanged() && sneaking) {
            horse.playAngrySound();
        }

        horse.setAngry(angry);
    }
}
