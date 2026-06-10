package tong.sihriya.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.client.vfx.composer.EffectComposer;
import tong.sihriya.client.vfx.emitter.Emitter;
import tong.sihriya.client.vfx.mesh.ProceduralMesh;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VFXEffect {
    private String vfxId;
    private final UUID effectId = UUID.randomUUID();
    private Vec3 position;
    private Vec3 direction;
    private int age;
    private int lifetime;
    private float intensity = 1.0f;
    private boolean active = true;

    private final List<Emitter> emitters = new ArrayList<>();
    private final List<ProceduralMesh> meshes = new ArrayList<>();
    private EffectComposer composer;
    private ResourceLocation texture;
    private float[] color = new float[]{1, 1, 1};
    private float alpha = 1.0f;
    private float scale = 1.0f;
    private boolean fade;

    public void tick() {
        if (!active) return;
        age++;
        if (age >= lifetime) { active = false; return; }

        if (composer != null) {
            composer.tick();
        } else {
            for (Emitter e : emitters) {
                if (position != null) e.setPosition(position);
                e.tick();
            }
        }
        if (fade) alpha = 1.0f - (float) age / lifetime;
    }

    public void render(PoseStack stack, float partialTick, MultiBufferSource buffer) {
        if (!active) return;

        if (composer != null) {
            composer.render(stack, partialTick, buffer);
            return;
        }

        if (meshes.isEmpty()) return;
        stack.pushPose();
        if (position != null) stack.translate(position.x, position.y, position.z);
        stack.scale(scale, scale, scale);
        float a = fade && lifetime > 0 ? alpha * (1 - (float)age/lifetime) : alpha;
        for (ProceduralMesh mesh : meshes) {
            if (texture != null) {
                mesh.render(stack, buffer, texture, color, a, 1.0f, fade);
            }
        }
        stack.popPose();
    }

    public void initComposer(ClientLevel level, Vec3 pos, String schoolId) {
        composer = new EffectComposer();
        composer.init(level, pos, schoolId);
    }

    public EffectComposer getComposer() { return composer; }
    public void addEmitter(Emitter e) { emitters.add(e); }
    public void addMesh(ProceduralMesh m) { meshes.add(m); }
    public List<Emitter> getEmitters() { return emitters; }
    public List<ProceduralMesh> getMeshes() { return meshes; }

    public void reset() {
        vfxId = null; age = 0; lifetime = 0; intensity = 1.0f; active = true;
        color = new float[]{1, 1, 1}; alpha = 1.0f; scale = 1.0f; fade = false;
        emitters.clear(); meshes.clear(); composer = null;
    }

    public boolean isFinished() {
        if (!active) return true;
        if (composer != null) return composer.isFinished();
        return age >= lifetime && emitters.stream().allMatch(Emitter::isFinished);
    }
    public UUID getEffectId() { return effectId; }
    public String getVfxId() { return vfxId; }
    public void setVfxId(String v) { this.vfxId = v; }
    public Vec3 getPosition() { return position; }
    public void setPosition(Vec3 v) { this.position = v; if (composer != null) composer.setPosition(v); }
    public Vec3 getDirection() { return direction; }
    public void setDirection(Vec3 v) { this.direction = v; }
    public int getAge() { return age; }
    public void setAge(int v) { this.age = v; }
    public int getLifetime() { return lifetime; }
    public void setLifetime(int v) { this.lifetime = v; }
    public float getIntensity() { return intensity; }
    public void setIntensity(float v) { this.intensity = v; }
    public void setColor(float[] c) { this.color = c; }
    public void setAlpha(float a) { this.alpha = a; }
    public void setScale(float s) { this.scale = s; }
    public void setFade(boolean f) { this.fade = f; }
    public void setTexture(ResourceLocation t) { this.texture = t; }
}
