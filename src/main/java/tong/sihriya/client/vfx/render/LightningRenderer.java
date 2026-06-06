package tong.sihriya.client.vfx.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import tong.sihriya.client.vfx.shader.SihriyaCoreShaders;

import java.util.List;

public class LightningRenderer {
    public static void renderBolt(PoseStack stack, Vec3 start, Vec3 end, float thickness,
                                   float[] color, float[] color2, float alpha, float time) {
        List<Vec3> points = LightningBoltHelper.generateBolt(start, end, 0.6f, 5);

        RenderSystem.setShader(() -> SihriyaCoreShaders.getGlowShader());
        SihriyaCoreShaders.setGlowUniforms(time, Vec3.ZERO, color, color2, 3.0f);
        RenderSystem.setShaderTexture(0, ProceduralTextureHelper.LIGHTNING_TEX);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.NEW_ENTITY);
        Matrix4f mat = stack.last().pose();

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p0 = points.get(i);
            Vec3 p1 = points.get(Math.min(i + 1, points.size() - 1));
            Vec3 dir = p1.subtract(p0).normalize();
            Vec3 up = Math.abs(dir.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
            Vec3 right = dir.cross(up).normalize();
            float w = thickness * (1 - Math.abs((float)i / (points.size() - 1) - 0.5f)) * 0.5f;

            for (int side : new int[]{-1, 1}) {
                float ox = (float)(right.x * w * side);
                float oy = (float)(right.y * w * side);
                float oz = (float)(right.z * w * side);
                float t = (float)i / (points.size() - 1);
                builder.vertex(mat, (float)p0.x + ox, (float)p0.y + oy, (float)p0.z + oz)
                    .color(color[0], color[1], color[2], alpha).uv(t, 0)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0, 1, 0).endVertex();
                builder.vertex(mat, (float)p1.x + ox, (float)p1.y + oy, (float)p1.z + oz)
                    .color(color[0], color[1], color[2], alpha).uv(t + 0.05f, 0)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0, 1, 0).endVertex();
            }
        }
        Tesselator.getInstance().end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
