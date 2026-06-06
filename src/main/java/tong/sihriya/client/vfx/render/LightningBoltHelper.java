package tong.sihriya.client.vfx.render;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LightningBoltHelper {
    private static final Random RANDOM = new Random();

    public static List<Vec3> generateBolt(Vec3 start, Vec3 end, float displacement, int detail) {
        List<Vec3> points = new ArrayList<>();
        points.add(start);
        points.add(end);

        for (int i = 0; i < detail; i++) {
            List<Vec3> subdivided = new ArrayList<>();
            subdivided.add(points.get(0));

            for (int j = 0; j < points.size() - 1; j++) {
                Vec3 p1 = points.get(j);
                Vec3 p2 = points.get(j + 1);
                Vec3 mid = p1.add(p2).scale(0.5);
                Vec3 dir = p2.subtract(p1).normalize();
                Vec3 perp = new Vec3(-dir.z, 0, dir.x);
                Vec3 up = new Vec3(0, 1, 0);

                double offset = (RANDOM.nextDouble() - 0.5) * displacement;
                mid = mid.add(perp.scale(offset * 0.7)).add(up.scale(offset * 0.3));

                subdivided.add(mid);
                subdivided.add(p2);
            }
            points = subdivided;
            displacement *= 0.5f;
        }

        return points;
    }
}
