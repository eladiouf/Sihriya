package tong.sihriya.client.vfx.emitter;

import net.minecraft.world.phys.Vec3;

public class ConeEmitter extends Emitter {
    private final float angle;
    private final float speed;
    private final int count;

    public ConeEmitter(float angle, float speed, int count, int lifetime) {
        this.angle = angle;
        this.speed = speed;
        this.count = count;
        this.lifetime = lifetime;
    }

    @Override
    protected void emit() {
        for (int i = 0; i < count; i++) {
            float theta = RANDOM.nextFloat() * (float) Math.PI * 2;
            float phi = RANDOM.nextFloat() * angle - angle / 2;
            Vec3 dir = new Vec3(
                Math.cos(theta) * Math.sin(phi),
                Math.cos(phi),
                Math.sin(theta) * Math.sin(phi)
            ).normalize();
            float spd = speed * (0.5f + RANDOM.nextFloat() * 0.5f);
            Vec3 pos = position.add(dir.scale(0.2));
            spawnParticle(pos, dir.scale(spd), 1);
        }
    }
}
