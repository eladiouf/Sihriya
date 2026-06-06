package tong.sihriya.client.vfx.emitter;

import net.minecraft.world.phys.Vec3;

public class BurstEmitter extends Emitter {
    private final int count;
    private final float speed;
    private final float spread;

    public BurstEmitter(int count, float speed, float spread) {
        this.count = count;
        this.speed = speed;
        this.spread = spread;
        this.lifetime = 5;
    }

    @Override
    protected void emit() {
        for (int i = 0; i < count; i++) {
            float theta = RANDOM.nextFloat() * (float) Math.PI * 2;
            float phi = (float) Math.acos(2 * RANDOM.nextFloat() - 1);
            float spd = speed * (0.3f + RANDOM.nextFloat() * 0.7f);
            Vec3 dir = new Vec3(
                Math.sin(phi) * Math.cos(theta) * spd,
                Math.sin(phi) * Math.sin(theta) * spd,
                Math.cos(phi) * spd
            );
            Vec3 pos = position.add(
                (RANDOM.nextDouble() - 0.5) * spread,
                (RANDOM.nextDouble() - 0.5) * spread,
                (RANDOM.nextDouble() - 0.5) * spread
            );
            spawnParticle(pos, dir, 1);
        }
    }
}
