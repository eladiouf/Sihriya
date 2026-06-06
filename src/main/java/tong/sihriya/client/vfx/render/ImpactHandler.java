package tong.sihriya.client.vfx.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.registry.SihriyaParticles;

public class ImpactHandler {
    public static void spawnImpact(ClientLevel level, Vec3 pos, String schoolId) {
        SimpleParticleType type = SihriyaParticles.getForSchool(schoolId);

        for (int i = 0; i < 30; i++) {
            double theta = level.random.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * level.random.nextDouble() - 1);
            double speed = 0.3 + level.random.nextDouble() * 0.6;
            level.addParticle(type, true,
                pos.x + (level.random.nextDouble() - 0.5) * 0.3,
                pos.y + (level.random.nextDouble() - 0.5) * 0.3,
                pos.z + (level.random.nextDouble() - 0.5) * 0.3,
                Math.sin(phi) * Math.cos(theta) * speed,
                Math.sin(phi) * Math.sin(theta) * speed,
                Math.cos(phi) * speed);
        }

        for (int i = 0; i < 20; i++) {
            double angle = i * Math.PI * 2 / 20;
            double r = 0.5 + level.random.nextDouble() * 2.0;
            level.addParticle(type, true,
                pos.x + Math.cos(angle) * r, pos.y + 0.1, pos.z + Math.sin(angle) * r,
                0, 0.03, 0);
        }

        for (int i = 0; i < 10; i++) {
            level.addParticle(type, true,
                pos.x + (level.random.nextDouble() - 0.5) * 0.5,
                pos.y + 0.2 + level.random.nextDouble() * 1.5,
                pos.z + (level.random.nextDouble() - 0.5) * 0.5,
                0, 0.05, 0);
        }
    }
}
