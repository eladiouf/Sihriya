package tong.sihriya.client.vfx.composer;

import net.minecraft.resources.ResourceLocation;
import tong.sihriya.client.vfx.emitter.Emitter;
import tong.sihriya.client.vfx.mesh.ProceduralMesh;

import java.util.ArrayList;
import java.util.List;

public class EffectPhase {
    private final int startTick;
    private final int duration;
    private final List<Emitter> emitters = new ArrayList<>();
    private final List<ProceduralMesh> meshes = new ArrayList<>();
    private ResourceLocation texture;
    private float[] color;
    private float alpha = 1.0f;
    private float scale = 1.0f;
    private boolean fade;

    public EffectPhase(int startTick, int duration) {
        this.startTick = startTick;
        this.duration = duration;
    }

    public boolean isActive(int globalTick) {
        return globalTick >= startTick && globalTick < startTick + duration;
    }

    public float progress(int globalTick) {
        return (float) (globalTick - startTick) / duration;
    }

    public void addEmitter(Emitter e) { emitters.add(e); }
    public void addMesh(ProceduralMesh m) { meshes.add(m); }
    public List<Emitter> getEmitters() { return emitters; }
    public List<ProceduralMesh> getMeshes() { return meshes; }

    public int getStartTick() { return startTick; }
    public int getDuration() { return duration; }
    public ResourceLocation getTexture() { return texture; }
    public void setTexture(ResourceLocation t) { this.texture = t; }
    public float[] getColor() { return color; }
    public void setColor(float[] c) { this.color = c; }
    public float getAlpha() { return alpha; }
    public void setAlpha(float a) { this.alpha = a; }
    public float getScale() { return scale; }
    public void setScale(float s) { this.scale = s; }
    public boolean isFade() { return fade; }
    public void setFade(boolean f) { this.fade = f; }
}
