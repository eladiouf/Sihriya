package tong.sihriya.client.vfx.emitter;

import net.minecraft.world.phys.Vec3;

public class HelixEmitter extends Emitter {
    private final float radius;
    private final float height;
    private final float speed;
    private final int particlesPerTick;

    public HelixEmitter(float radius, float height, float speed, int particlesPerTick, int lifetime) {
        this.radius = radius;
        this.height = height;
        this.speed = speed;
        this.particlesPerTick = particlesPerTick;
        this.lifetime = lifetime;
    }

    @Override
    protected void emit() {
        float angle = age * speed;
        float baseY = (float) age / lifetime * height;
        for (int i = 0; i < particlesPerTick; i++) {
            float offset = (float) i * (float) Math.PI * 2 / particlesPerTick;
            Vec3 pos1 = position.add(
                Math.cos(angle + offset) * radius,
                baseY,
                Math.sin(angle + offset) * radius
            );
            spawnParticle(pos1, new Vec3(0, 0.01, 0), 1);
        }
    }
}
