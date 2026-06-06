package tong.sihriya.client.vfx.emitter;

import net.minecraft.world.phys.Vec3;

public class VortexEmitter extends Emitter {
    private final float radius;
    private final float height;
    private final float speed;
    private final float contractionSpeed;
    private final int particlesPerTick;

    public VortexEmitter(float radius, float height, float speed, float contractionSpeed,
                          int particlesPerTick, int lifetime) {
        this.radius = radius;
        this.height = height;
        this.speed = speed;
        this.contractionSpeed = contractionSpeed;
        this.particlesPerTick = particlesPerTick;
        this.lifetime = lifetime;
    }

    @Override
    protected void emit() {
        for (int i = 0; i < particlesPerTick; i++) {
            float angle = age * speed + i * 1.5f;
            float r = radius * (1 - (float) age / lifetime * contractionSpeed);
            float y = height * (float) age / lifetime;
            Vec3 pos = position.add(Math.cos(angle) * r, y, Math.sin(angle) * r);
            Vec3 vel = new Vec3(-Math.cos(angle) * 0.03, 0.05, -Math.sin(angle) * 0.03);
            spawnParticle(pos, vel, 1);
        }
    }
}
