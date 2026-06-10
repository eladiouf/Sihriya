package tong.sihriya.client.vfx.emitter;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.registry.SihriyaParticles;

import java.util.Random;

public abstract class Emitter {
    protected static final Random RANDOM = new Random();

    protected ClientLevel level;
    protected Vec3 position;
    protected String schoolId;
    protected int age;
    protected int lifetime;
    protected boolean active = true;

    public void init(ClientLevel level, Vec3 position, String schoolId, int lifetime) {
        this.level = level;
        this.position = position;
        this.schoolId = schoolId;
        this.age = 0;
        this.lifetime = lifetime;
        this.active = true;
    }

    public void tick() {
        if (!active) return;
        age++;
        if (age >= lifetime) { active = false; return; }
        emit();
    }

    protected abstract void emit();

    protected void spawnParticle(Vec3 pos, Vec3 vel, int count) {
        if (level == null) return;
        ParticleOptions type = SihriyaParticles.getForSchool(schoolId);
        for (int i = 0; i < count; i++) {
            level.addParticle(type, true,
                pos.x, pos.y, pos.z,
                vel.x, vel.y, vel.z);
        }
    }

    protected Vec3 randomVelocity(float speed, float spread) {
        return new Vec3(
            (RANDOM.nextDouble() - 0.5) * spread * speed,
            (RANDOM.nextDouble() - 0.5) * spread * speed,
            (RANDOM.nextDouble() - 0.5) * spread * speed
        );
    }

    public boolean isFinished() { return !active || age >= lifetime; }
    public void setPosition(Vec3 pos) { this.position = pos; }
}
