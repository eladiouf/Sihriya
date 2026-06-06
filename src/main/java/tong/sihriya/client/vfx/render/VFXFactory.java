package tong.sihriya.client.vfx.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.Sihriya;
import tong.sihriya.client.vfx.VFXEffect;
import tong.sihriya.client.vfx.emitter.*;
import tong.sihriya.client.vfx.mesh.SphereMesh;
import tong.sihriya.client.vfx.mesh.TorusMesh;
import tong.sihriya.data.SchoolColors;
import tong.sihriya.vfx.VFXDefinition;

public class VFXFactory {
    private static final ResourceLocation TEXTURE = ProceduralTextureHelper.MAGIC_GLOW;

    public static VFXEffect createEffect(VFXDefinition def, ClientLevel level, Vec3 pos, String schoolId) {
        VFXEffect effect = new VFXEffect();
        effect.setPosition(pos);
        effect.setLifetime(60);
        effect.setColor(SchoolColors.get(schoolId));
        effect.setTexture(TEXTURE);

        if (def == null) return effect;

        if (def.projectile() != null) {
            VFXDefinition.ProjectileConfig pc = def.projectile();
            effect.setColor(pc.color1() != null ? pc.color1() : SchoolColors.get(schoolId));
            effect.setTexture(ProceduralTextureHelper.STAR_BURST);
            effect.setScale(pc.scale());

            effect.addMesh(new SphereMesh(1.0f, 10, 10));

            float trailIntensity = pc.glowIntensity() > 0 ? pc.glowIntensity() : 1.5f;
            if (trailIntensity > 1.5f) {
                HelixEmitter trail = new HelixEmitter(0.4f, 0.15f, 0.18f, 4, 15);
                trail.init(level, pos, schoolId, 15);
                effect.addEmitter(trail);
            } else {
                HelixEmitter trail = new HelixEmitter(0.3f, 0.1f, 0.15f, 3, 12);
                trail.init(level, pos, schoolId, 12);
                effect.addEmitter(trail);
            }
        }

        if (def.impact() != null) {
            VFXDefinition.ImpactConfig ic = def.impact();
            if (ic.burst() != null) {
                VFXDefinition.EmitterConfig bc = ic.burst();
                BurstEmitter burst = new BurstEmitter(
                    bc.count() > 0 ? bc.count() : 20,
                    bc.speed() > 0 ? bc.speed() : 0.5f, 0.3f);
                burst.init(level, pos, schoolId, 5);
                effect.addEmitter(burst);
            }
            if (ic.shockwave()) {
                effect.setAlpha(0.3f);
                effect.addMesh(new TorusMesh(0.5f, 0.1f, 24, 8));
            }
        }

        return effect;
    }
}
