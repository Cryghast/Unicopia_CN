package com.minelittlepony.unicopia.particle;

import java.util.Locale;

import org.joml.Vector3f;

import com.minelittlepony.common.util.Color;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.Registries;

public class SphereParticleEffect implements ParticleEffect {
    @SuppressWarnings("deprecation")
    public static final Factory<SphereParticleEffect> FACTORY = ParticleFactoryHelper.of(SphereParticleEffect::new, SphereParticleEffect::new);

    private static final Vec3d DEFAULT_OFFSET = new Vec3d(0, 0.5, 0);

    private final Vector3f color;
    private final float alpha;
    private final float radius;

    private Vec3d offset = Vec3d.ZERO;

    private final ParticleType<? extends SphereParticleEffect> type;

    protected SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, StringReader reader) throws CommandSyntaxException {
        this(type, AbstractDustParticleEffect.readColor(reader), ParticleFactoryHelper.readFloat(reader), ParticleFactoryHelper.readFloat(reader), ParticleFactoryHelper.readVector(reader));
    }

    protected SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, PacketByteBuf buf) {
        this(type, AbstractDustParticleEffect.readColor(buf), buf.readFloat(), buf.readFloat());
    }

    public SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, int tint, float alpha, float rad) {
        this(type, tint, alpha, rad, DEFAULT_OFFSET);
    }

    public SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, Vector3f color, float alpha, float rad) {
        this(type, color, alpha, rad, DEFAULT_OFFSET);
    }

    public SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, int tint, float alpha, float rad, Vec3d offset) {
        this(type, new Vector3f(Color.r(tint) * 255, Color.g(tint) * 255, Color.b(tint) * 255), alpha, rad, offset);
    }

    public SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, Vector3f color, float alpha, float rad, Vec3d offset) {
        this.type = type;
        this.color = color;
        this.offset = offset;
        this.alpha = alpha;
        this.radius = rad;
    }

    public Vec3d getOffset() {
        return offset;
    }

    public void setOffset(Vec3d offset) {
        this.offset = offset;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getAlpha() {
        return alpha;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(color.x);
        buf.writeFloat(color.y);
        buf.writeFloat(color.z);
        buf.writeFloat(alpha);
        buf.writeFloat(radius);
        buf.writeDouble(offset.getX());
        buf.writeDouble(offset.getY());
        buf.writeDouble(offset.getZ());
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f %.2f",
                Registries.PARTICLE_TYPE.getId(getType()),
                color.x, color.y, color.z,
                alpha,
                radius,
                offset.getX(), offset.getY(), offset.getZ()
        );
    }
}
