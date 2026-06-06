package tong.sihriya.client.vfx.emitter;

import net.minecraft.world.phys.Vec3;

public class SpiralEmitter extends Emitter {
    private final float radius;
    private final float height;
    private final float speed;
    private final int particlesPerTick;

    public SpiralEmitter(float radius, float height, float speed, int particlesPerTick, int lifetime) {
        this.radius = radius;
        this.height = height;
        this.speed = speed;
        this.particlesPerTick = particlesPerTick;
        this.lifetime = lifetime;
    }

    @Override
    protected void emit() {
        float progress = (float) age / lifetime;
        for (int i = 0; i < particlesPerTick; i++) {
            float angle = age * speed + (float) i / particlesPerTick * (float) Math.PI * 2;
            float yOff = progress * height;
            float r = radius * (1 - progress * 0.3f);
            Vec3 pos = position.add(Math.cos(angle) * r, yOff, Math.sin(angle) * r);
            Vec3 vel = new Vec3(-Math.sin(angle) * 0.02, 0.01, Math.cos(angle) * 0.02);
            spawnParticle(pos, vel, 1);
        }
    }
}
