package tong.sihriya.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class SchoolGlowParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprite;
    private final float r, g, b;

    public SchoolGlowParticleProvider(SpriteSet sprite, float r, float g, float b) {
        this.sprite = sprite;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public SchoolGlowParticleProvider(SpriteSet sprite, float[] rgb) {
        this(sprite, rgb[0], rgb[1], rgb[2]);
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                   double x, double y, double z,
                                   double dx, double dy, double dz) {
        SchoolGlowParticle p = new SchoolGlowParticle(level, x, y, z, r, g, b, sprite);
        p.pickSprite(sprite);
        return p;
    }
}
