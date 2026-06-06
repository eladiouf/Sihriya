package tong.sihriya.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VFXEngine {
    private static VFXEngine INSTANCE;
    private final List<VFXEffect> activeEffects = new ArrayList<>();
    private final List<VFXEffect> pendingAdd = new ArrayList<>();
    private final ObjectPool<VFXEffect> effectPool = new ObjectPool<>(VFXEffect::new, 100);
    private final PerformanceMonitor perfMonitor = new PerformanceMonitor();

    private VFXEngine() {}

    public static void init() { INSTANCE = new VFXEngine(); }
    public static VFXEngine getInstance() { return INSTANCE; }

    public void tick() {
        perfMonitor.tick();
        activeEffects.addAll(pendingAdd);
        pendingAdd.clear();
        activeEffects.removeIf(e -> {
            e.tick();
            if (e.isFinished()) { effectPool.release(e); return true; }
            return false;
        });
    }

    public void render(PoseStack stack, float partialTick) {
        if (activeEffects.isEmpty()) return;
        if (tong.sihriya.client.vfx.shader.SihriyaCoreShaders.getGlowShader() == null) {
            return;
        }
        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        for (VFXEffect effect : activeEffects) {
            effect.render(stack, partialTick, bufferSource);
        }
        bufferSource.endBatch();
    }

    public void startEffect(VFXEffect effect) { pendingAdd.add(effect); }
    public void stopEffect(UUID id) { activeEffects.removeIf(e -> e.getEffectId().equals(id)); }
    public void clearAll() { activeEffects.clear(); pendingAdd.clear(); }
}
