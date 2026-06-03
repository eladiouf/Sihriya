package tong.sihriya.client.particle.magiccircle;

import net.minecraft.world.phys.Vec3;

public class CircleShape {

    public static Vec3[] circlePoints(double radius, int count, double startAngle) {
        Vec3[] points = new Vec3[count];
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(startAngle) + (2 * Math.PI * i / count);
            points[i] = new Vec3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        }
        return points;
    }

    public static Vec3[] spiralPoints(double maxRadius, int count, double startAngle, double turns) {
        Vec3[] points = new Vec3[count];
        for (int i = 0; i < count; i++) {
            double progress = (double) i / count;
            double r = progress * maxRadius;
            double angle = Math.toRadians(startAngle) + turns * 2 * Math.PI * progress;
            points[i] = new Vec3(Math.cos(angle) * r, 0, Math.sin(angle) * r);
        }
        return points;
    }

    public static Vec3[][] doubleRing(double innerR, double outerR, int count) {
        return new Vec3[][] {
            circlePoints(innerR, count / 2, 0),
            circlePoints(outerR, count / 2, 180.0 / count)
        };
    }
}
