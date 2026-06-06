package tong.sihriya.client.vfx.composer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.client.vfx.emitter.Emitter;

import java.util.ArrayList;
import java.util.List;

public class EffectComposer {
    private final List<EffectPhase> phases = new ArrayList<>();
    private int globalTick;
    private Vec3 position;
    private ClientLevel level;

    public void init(ClientLevel level, Vec3 position, String schoolId) {
        this.level = level;
        this.position = position;
        this.globalTick = 0;
    }

    public void addPhase(EffectPhase phase) {
        phases.add(phase);
    }

    public void tick() {
        globalTick++;
        for (EffectPhase phase : phases) {
            if (phase.isActive(globalTick)) {
                for (Emitter e : phase.getEmitters()) {
                    if (position != null) e.setPosition(position);
                    e.tick();
                }
            }
        }
    }

    public void render(PoseStack stack, float partialTick, MultiBufferSource buffer) {
        stack.pushPose();
        if (position != null) stack.translate(position.x, position.y, position.z);
        for (EffectPhase phase : phases) {
            if (phase.isActive(globalTick)) {
                float p = phase.progress(globalTick);
                float a = phase.isFade() ? phase.getAlpha() * (1 - p) : phase.getAlpha();
                float s = phase.getScale();
                stack.pushPose();
                stack.scale(s, s, s);
                for (var mesh : phase.getMeshes()) {
                    if (phase.getTexture() != null) {
                        mesh.render(stack, buffer, phase.getTexture(),
                            phase.getColor(), a, 1.0f, phase.isFade());
                    }
                }
                stack.popPose();
            }
        }
        stack.popPose();
    }

    public void setPosition(Vec3 pos) { this.position = pos; }
    public boolean isFinished() {
        return phases.isEmpty() || globalTick >= phases.stream()
            .mapToInt(p -> p.getStartTick() + p.getDuration()).max().orElse(0);
    }
    public int getGlobalTick() { return globalTick; }
}
