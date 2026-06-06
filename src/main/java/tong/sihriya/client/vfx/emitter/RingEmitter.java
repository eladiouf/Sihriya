package tong.sihriya.client.vfx.emitter;

import net.minecraft.world.phys.Vec3;

public class RingEmitter extends Emitter {
    private final float startRadius;
    private final float endRadius;
    private final float speed;
    private final int particles;

    public RingEmitter(float startRadius, float endRadius, float speed, int particles, int lifetime) {
        this.startRadius = startRadius;
        this.endRadius = endRadius;
        this.speed = speed;
        this.particles = particles;
        this.lifetime = lifetime;
    }

    @Override
    protected void emit() {
        float progress = (float) age / lifetime;
        float r = startRadius + (endRadius - startRadius) * progress;
        for (int i = 0; i < particles; i++) {
            float theta = (float) i / particles * (float) Math.PI * 2;
            Vec3 pos = position.add(Math.cos(theta) * r, 0.1, Math.sin(theta) * r);
            spawnParticle(pos, Vec3.ZERO, 1);
        }
    }
}
