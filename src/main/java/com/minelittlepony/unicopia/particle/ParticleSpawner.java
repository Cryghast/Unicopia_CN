package com.minelittlepony.unicopia.particle;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

public interface ParticleSpawner {
    ParticleSpawner EMPTY = (effect, pos, vel) -> {};

    void addParticle(ParticleEffect effect, Vec3d position, Vec3d velocity);
}
