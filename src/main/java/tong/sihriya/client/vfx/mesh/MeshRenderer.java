package tong.sihriya.client.vfx.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import tong.sihriya.client.vfx.shader.SihriyaRenderTypes;
import java.util.List;

public class MeshRenderer {
    public static void render(PoseStack stack, MultiBufferSource buffer,
                               List<MeshVertex> vertices, ResourceLocation texture,
                               float[] color, float alpha, float scale, boolean fade) {
        VertexConsumer consumer = buffer.getBuffer(SihriyaRenderTypes.glowMesh(texture));
        Matrix4f mat = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        stack.pushPose();
        stack.scale(scale, scale, scale);
        mat = stack.last().pose();
        for (MeshVertex v : vertices) {
            consumer.vertex(mat, v.x(), v.y(), v.z())
                .color(color[0], color[1], color[2], alpha)
                .uv(v.u(), v.v())
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(normal, v.nx(), v.ny(), v.nz())
                .endVertex();
        }
        stack.popPose();
    }
}
