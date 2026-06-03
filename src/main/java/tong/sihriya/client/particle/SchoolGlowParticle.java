package tong.sihriya.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class SchoolGlowParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public SchoolGlowParticle(ClientLevel level, double x, double y, double z,
                              float r, float g, float b, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprites;
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.gravity = 0;
        this.lifetime = 20 + random.nextInt(10);
        this.quadSize = 0.3f + random.nextFloat() * 0.4f;
        this.hasPhysics = false;
        this.friction = 0.99f;
        this.xd = (random.nextDouble() - 0.5) * 0.1;
        this.zd = (random.nextDouble() - 0.5) * 0.1;
        this.yd = random.nextDouble() * 0.02;
    }

    @Override
    public void tick() {
        super.tick();
        this.yd += Math.sin(age * 0.2) * 0.002;
        float fade = 1.0f - (float) age / lifetime;
        this.alpha = Math.max(0, fade);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }
}
