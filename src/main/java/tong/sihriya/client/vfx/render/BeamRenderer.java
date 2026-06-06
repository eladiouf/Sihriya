package tong.sihriya.client.vfx.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import tong.sihriya.client.vfx.shader.SihriyaCoreShaders;

public class BeamRenderer {
    private static final ResourceLocation TEXTURE = ProceduralTextureHelper.BEAM_STREAK;

    public static void renderBeam(PoseStack stack, Vec3 from, Vec3 to, float thickness,
                                   float[] color, float[] color2, float alpha, float time) {
        Vec3 dir = to.subtract(from).normalize();
        Vec3 up = Math.abs(dir.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 right = dir.cross(up).normalize();

        RenderSystem.setShader(() -> SihriyaCoreShaders.getGlowShader());
        SihriyaCoreShaders.setGlowUniforms(time, Vec3.ZERO, color, color2, 2.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        Matrix4f mat = stack.last().pose();

        int segments = 16;
        for (int i = 0; i < segments; i++) {
            float t0 = (float) i / segments;
            float t1 = (float) (i + 1) / segments;
            Vec3 p0 = from.add(to.subtract(from).scale(t0));
            Vec3 p1 = from.add(to.subtract(from).scale(t1));
            float w0 = thickness * (1 - Math.abs(t0 - 0.5f) * 2) * 0.5f;
            float w1 = thickness * (1 - Math.abs(t1 - 0.5f) * 2) * 0.5f;
            Vec3 r0 = right.scale(w0);
            Vec3 r1 = right.scale(w1);
            float u = time * 0.15f;

            builder.vertex(mat, (float)(p0.x - r0.x), (float)(p0.y - r0.y), (float)(p0.z - r0.z))
                .color(color[0], color[1], color[2], alpha).uv(u, t0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0, 1, 0).endVertex();
            builder.vertex(mat, (float)(p0.x + r0.x), (float)(p0.y + r0.y), (float)(p0.z + r0.z))
                .color(color[0], color[1], color[2], alpha).uv(u, t1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0, 1, 0).endVertex();
            builder.vertex(mat, (float)(p1.x + r1.x), (float)(p1.y + r1.y), (float)(p1.z + r1.z))
                .color(color[0], color[1], color[2], alpha).uv(u + 0.15f, t1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0, 1, 0).endVertex();
            builder.vertex(mat, (float)(p1.x - r1.x), (float)(p1.y - r1.y), (float)(p1.z - r1.z))
                .color(color[0], color[1], color[2], alpha).uv(u + 0.15f, t0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0, 1, 0).endVertex();
        }
        Tesselator.getInstance().end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
