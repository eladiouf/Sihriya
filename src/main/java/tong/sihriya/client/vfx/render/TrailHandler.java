package tong.sihriya.client.vfx.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.registry.SihriyaParticles;

public class TrailHandler {
    private final ClientLevel level;
    private final String schoolId;
    private final int particlesPerTick;
    private final int particleLifetime;
    private final float spread;

    public TrailHandler(ClientLevel level, String schoolId, int particlesPerTick,
                         int particleLifetime, float spread) {
        this.level = level;
        this.schoolId = schoolId;
        this.particlesPerTick = particlesPerTick;
        this.particleLifetime = particleLifetime;
        this.spread = spread;
    }

    public void emit(Vec3 position) {
        SimpleParticleType type = SihriyaParticles.getForSchool(schoolId);
        for (int i = 0; i < particlesPerTick; i++) {
            double dx = (level.random.nextDouble() - 0.5) * spread;
            double dy = level.random.nextDouble() * spread * 0.5;
            double dz = (level.random.nextDouble() - 0.5) * spread;
            level.addParticle(type, true,
                position.x + dx, position.y + dy, position.z + dz,
                0, 0.01, 0);
        }
    }
}
