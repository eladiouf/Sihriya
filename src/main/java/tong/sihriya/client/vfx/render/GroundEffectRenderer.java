package tong.sihriya.client.vfx.render;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.Sihriya;
import tong.sihriya.client.vfx.VFXEffect;
import tong.sihriya.client.vfx.emitter.RingEmitter;
import tong.sihriya.client.vfx.emitter.SpiralEmitter;
import tong.sihriya.client.vfx.mesh.DiskMesh;
import tong.sihriya.client.vfx.mesh.TorusMesh;
import tong.sihriya.data.SchoolColors;

public class GroundEffectRenderer {

    public static VFXEffect createGroundMark(Vec3 pos, String schoolId, float radius, int lifetime) {
        VFXEffect effect = new VFXEffect();
        effect.setPosition(pos);
        effect.setLifetime(lifetime);
        effect.setColor(SchoolColors.get(schoolId));
        effect.setTexture(ProceduralTextureHelper.TWIRL);
        effect.setAlpha(0.25f);
        effect.setScale(radius);
        effect.setFade(true);

        effect.addMesh(new DiskMesh(1.0f, 6, 24));
        effect.addMesh(new TorusMesh(0.9f, 0.04f, 24, 8));

        RingEmitter ring = new RingEmitter(0.5f, radius, 0.25f, 12, lifetime);
        SpiralEmitter spiral = new SpiralEmitter(radius, 1.0f, 0.1f, 3, lifetime);

        effect.addEmitter(ring);
        effect.addEmitter(spiral);

        return effect;
    }
}
