package tong.sihriya.client.vfx.render;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.Sihriya;
import tong.sihriya.client.vfx.VFXEffect;
import tong.sihriya.client.vfx.emitter.HelixEmitter;
import tong.sihriya.client.vfx.emitter.SpiralEmitter;
import tong.sihriya.client.vfx.mesh.DiskMesh;
import tong.sihriya.client.vfx.mesh.SphereMesh;
import tong.sihriya.client.vfx.mesh.TorusMesh;
import tong.sihriya.data.SchoolColors;

public class AuraRenderer {

    public static VFXEffect createAura(Vec3 pos, String schoolId, float radius, int lifetime) {
        VFXEffect effect = new VFXEffect();
        effect.setPosition(pos);
        effect.setLifetime(lifetime);
        effect.setColor(SchoolColors.get(schoolId));
        effect.setTexture(ProceduralTextureHelper.CIRCLE_RINGS);
        effect.setAlpha(0.2f);
        effect.setScale(radius);

        effect.addMesh(new TorusMesh(radius, 0.08f, 24, 12));
        effect.addMesh(new SphereMesh(radius * 0.85f, 8, 8));

        HelixEmitter helix = new HelixEmitter(radius * 1.1f, 0.2f, 0.12f, 4, lifetime);
        SpiralEmitter spiral = new SpiralEmitter(radius, 1.5f, 0.08f, 3, lifetime);

        effect.addEmitter(helix);
        effect.addEmitter(spiral);

        return effect;
    }

    public static VFXEffect createShield(Vec3 pos, String schoolId, float radius, int lifetime) {
        VFXEffect effect = new VFXEffect();
        effect.setPosition(pos);
        effect.setLifetime(lifetime);
        effect.setColor(SchoolColors.get(schoolId));
        effect.setTexture(ProceduralTextureHelper.SHIELD_MASK);
        effect.setAlpha(0.4f);
        effect.setScale(radius);
        effect.setFade(true);

        effect.addMesh(new SphereMesh(1.0f, 16, 16));
        effect.addMesh(new TorusMesh(1.15f, 0.06f, 24, 12));

        return effect;
    }
}
