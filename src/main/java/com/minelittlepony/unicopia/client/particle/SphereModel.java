package com.minelittlepony.unicopia.client.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.client.gui.DrawableUtil;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;

public class SphereModel {
    public static final SphereModel SPHERE = new SphereModel(40, 40, DrawableUtil.TAU);
    public static final SphereModel DISK = new SphereModel(40, 2, DrawableUtil.PI);
    public static final SphereModel HEXAGON = new SphereModel(3, 2, DrawableUtil.TAU);
    public static final SphereModel PRISM = new SphereModel(6, 6, DrawableUtil.TAU);

    private final List<Vector4f> vertices = new ArrayList<>();
    private final Vector4f drawVert = new Vector4f();

    public SphereModel(double rings, double sectors, double azimuthRange) {
        double zenithIncrement = DrawableUtil.PI / rings;
        double azimuthIncrement = DrawableUtil.TAU / sectors;
        compileVertices(azimuthRange, zenithIncrement, azimuthIncrement, vertices::add);
    }

    public final void render(MatrixStack matrices, VertexConsumer vertexWriter, int light, int overlay, float radius, float r, float g, float b, float a) {
        radius = Math.abs(radius);
        if (radius < 0.001F) {
            return;
        }

        Matrix4f model = matrices.peek().getPositionMatrix();
        for (Vector4f vertex : vertices) {
            drawVert.set(vertex.x * radius, vertex.y * radius, vertex.z * radius, vertex.w);
            drawVert.mul(model);
            vertexWriter.vertex(drawVert.x, drawVert.y, drawVert.z, r, g, b, a, 0, 0, overlay, light, 0, 0, 0);
        }
    }

    private static void compileVertices(double azimuthRange, double zenithIncrement, double azimuthIncrement, Consumer<Vector4f> collector) {
        for (double zenith = 0; zenith < DrawableUtil.PI; zenith += zenithIncrement) {
            for (double azimuth = 0; azimuth < azimuthRange; azimuth += azimuthIncrement) {
                collector.accept(convertToCartesianCoord(new Vector4f(), 1, zenith, azimuth));
                collector.accept(convertToCartesianCoord(new Vector4f(), 1, zenith + zenithIncrement, azimuth));
                collector.accept(convertToCartesianCoord(new Vector4f(), 1, zenith + zenithIncrement, azimuth + azimuthIncrement));
                collector.accept(convertToCartesianCoord(new Vector4f(), 1, zenith, azimuth + azimuthIncrement));
            }
        }
    }

    public static Vector4f convertToCartesianCoord(Vector4f output, double r, double theta, double phi) {
        float st = MathHelper.sin((float)theta);
        output.set(
            (float)(r * st * MathHelper.cos((float)phi)),
            (float)(r * st * MathHelper.sin((float)phi)),
            (float)(r * MathHelper.cos((float)theta)),
            1
        );
        return output;
    }
}
