package tong.sihriya.client.particle;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.registry.SihriyaParticles;

import java.util.Random;

public class SpellParticleHelper {
    private static final Random RANDOM = new Random();

    /** Spawns a full burst of vanilla particles forming a magic circle */
    public static void spawnMagicCircleBurst(Level level, Vec3 pos, String schoolId) {
        if (!level.isClientSide) return;
        var particle = SihriyaParticles.getForSchool(schoolId);
        double radius = 3.5;
        double baseY = pos.y;

        // 4 concentric rings at different heights
        for (int h = 0; h < 4; h++) {
            double yOff = 0.1 + h * 0.18;
            int count = 24 + h * 6;
            double r = radius - h * 0.3;
            for (int i = 0; i < count; i++) {
                double a = i * (Math.PI * 2 / count) + h * 0.3;
                double x = pos.x + Math.cos(a) * r;
                double z = pos.z + Math.sin(a) * r;
                level.addParticle(particle, true, x, baseY + yOff, z,
                    0, 0.01 + h * 0.004, 0);
            }
        }

        // Inner counter-ring
        int innerCount = 20;
        double innerR = radius * 0.5;
        for (int i = 0; i < innerCount; i++) {
            double a = i * (Math.PI * 2 / innerCount) + 0.5;
            double x = pos.x + Math.cos(a) * innerR;
            double z = pos.z + Math.sin(a) * innerR;
            level.addParticle(particle, true, x, baseY + 0.3, z,
                0, 0.018, 0);
        }

        // Rising sparkles inside the circle
        for (int i = 0; i < 30; i++) {
            double a = RANDOM.nextDouble() * Math.PI * 2;
            double r = RANDOM.nextDouble() * radius * 0.9;
            double x = pos.x + Math.cos(a) * r;
            double z = pos.z + Math.sin(a) * r;
            level.addParticle(particle, true,
                x, baseY + 0.1 + RANDOM.nextDouble() * 0.5, z,
                0, 0.03 + RANDOM.nextDouble() * 0.04, 0);
        }
    }

    public static void spawnGlowBurst(Level level, Vec3 pos, String schoolId, int count) {
        var particle = SihriyaParticles.getForSchool(schoolId);
        for (int i = 0; i < count; i++) {
            double dx = (RANDOM.nextDouble() - 0.5) * 2;
            double dy = RANDOM.nextDouble() * 1.5;
            double dz = (RANDOM.nextDouble() - 0.5) * 2;
            level.addParticle(particle, true,
                pos.x + dx, pos.y + dy, pos.z + dz,
                0, 0.02, 0);
        }
    }
}
