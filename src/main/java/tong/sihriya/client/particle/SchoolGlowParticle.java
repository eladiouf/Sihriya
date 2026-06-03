package tong.sihriya.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class SchoolGlowParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseSize;

    public SchoolGlowParticle(ClientLevel level, double x, double y, double z,
                              float r, float g, float b, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprites;
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.gravity = 0;
        this.lifetime = 25 + random.nextInt(15);
        this.baseSize = 0.15f + random.nextFloat() * 0.35f;
        this.quadSize = baseSize;
        this.hasPhysics = false;
        this.friction = 0.98f;
        this.xd = (random.nextDouble() - 0.5) * 0.08;
        this.zd = (random.nextDouble() - 0.5) * 0.08;
        this.yd = random.nextFloat() * 0.03 + 0.01;
    }

    @Override
    public void tick() {
        super.tick();
        float progress = (float) age / lifetime;

        // Sinusoidal floating
        this.yd += Math.sin(age * 0.3) * 0.0015;

        // Size pulse: small → big → small
        float pulse = (float) Math.sin(progress * Math.PI);
        this.quadSize = baseSize * (0.6f + 0.4f * pulse);

        // Alpha: fade in, then fade out
        if (progress < 0.15f) {
            this.alpha = progress / 0.15f;
        } else if (progress > 0.7f) {
            this.alpha = Math.max(0, (1.0f - progress) / 0.3f);
        } else {
            this.alpha = 1.0f;
        }

        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }
}
