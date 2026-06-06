package tong.sihriya.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

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
        this.lifetime = 35 + random.nextInt(20);
        this.baseSize = 0.3f + random.nextFloat() * 0.5f;
        this.quadSize = baseSize;
        this.hasPhysics = false;
        this.friction = 0.97f;
        this.xd = (random.nextDouble() - 0.5) * 0.06;
        this.zd = (random.nextDouble() - 0.5) * 0.06;
        this.yd = 0.02f + random.nextFloat() * 0.03f;
    }

    @Override
    public void tick() {
        super.tick();
        float progress = (float) age / lifetime;
        this.yd += Math.sin(age * 0.2) * 0.002;
        float pulse = (float) Math.sin(progress * Math.PI);
        this.quadSize = baseSize * (0.5f + 0.5f * pulse);
        if (progress < 0.1f) this.alpha = progress / 0.1f;
        else if (progress > 0.7f) this.alpha = Math.max(0, (1.0f - progress) / 0.3f);
        else this.alpha = 1.0f;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ADDITIVE;
    }

    private static final ParticleRenderType ADDITIVE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager texManager) {
            texManager.getTexture(TextureAtlas.LOCATION_PARTICLES).setBlurMipmap(true, false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    };
}
