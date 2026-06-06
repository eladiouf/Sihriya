package tong.sihriya.client.vfx.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.function.Function;

public final class SihriyaRenderUtils {

    private static final ResourceLocation BLOCK_ATLAS =
        new ResourceLocation("minecraft", "textures/atlas/blocks.png");

    public static void drawCube(VertexConsumer vc, PoseStack pose, int light,
                                 float r, float g, float b, float a,
                                 ResourceLocation tex, float height, Matrix4f rot,
                                 boolean doubleSided, boolean top, boolean bottom) {
        drawCube(vc, pose, light, r, g, b, a, tex, tex, height, rot, doubleSided, top, bottom);
    }

    public static void drawCube(VertexConsumer vc, PoseStack pose, int light,
                                 float r, float g, float b, float a,
                                 ResourceLocation tex, ResourceLocation topTex, float height,
                                 Matrix4f rot, boolean doubleSided, boolean top, boolean bottom) {
        Function<ResourceLocation, TextureAtlasSprite> atlas =
            Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS);

        TextureAtlasSprite s = atlas.apply(tex);
        float u0 = s.getU0(), u1 = s.getU1(), v0 = s.getV0(), v1 = s.getV1();

        float m = 0.5f;
        Vector4f p1 = new Vector4f(-m,  m, height, 1).mul(rot);
        Vector4f p2 = new Vector4f(-m, -m, height, 1).mul(rot);
        Vector4f p3 = new Vector4f( m, -m, height, 1).mul(rot);
        Vector4f p4 = new Vector4f( m,  m, height, 1).mul(rot);
        Vector4f p5 = new Vector4f(-m, -m, 0,      1).mul(rot);
        Vector4f p6 = new Vector4f(-m,  m, 0,      1).mul(rot);
        Vector4f p7 = new Vector4f( m,  m, 0,      1).mul(rot);
        Vector4f p8 = new Vector4f( m, -m, 0,      1).mul(rot);

        quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a, -1, 0, 0, p5, p2, p1, p6);
        quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a, -1, 0, 0, p7, p4, p3, p8);
        quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a,  0, 1, 0, p6, p1, p4, p7);
        quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a, -1, 0, 0, p8, p3, p2, p5);

        if (tex != topTex) {
            TextureAtlasSprite ts = atlas.apply(topTex);
            u0 = ts.getU0(); u1 = ts.getU1(); v0 = ts.getV0(); v1 = ts.getV1();
        }
        if (top)    quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a, 0, 0, 1, p5, p6, p7, p8);
        if (bottom) quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a, 0, 0, 1, p1, p2, p3, p4);

        if (doubleSided) drawInvertedCube(vc, pose, light, r, g, b, a, tex, topTex, height, rot, top, bottom);
    }

    public static void drawInvertedCube(VertexConsumer vc, PoseStack pose, int light,
                                         float r, float g, float b, float a,
                                         ResourceLocation tex, ResourceLocation topTex, float height,
                                         Matrix4f rot, boolean top, boolean bottom) {
        Function<ResourceLocation, TextureAtlasSprite> atlas =
            Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS);

        TextureAtlasSprite s = atlas.apply(tex);
        float u0 = s.getU0(), u1 = s.getU1(), v0 = s.getV0(), v1 = s.getV1();

        float m = 0.5f;
        Vector4f p1 = new Vector4f(-m,  m, height, 1).mul(rot);
        Vector4f p2 = new Vector4f(-m, -m, height, 1).mul(rot);
        Vector4f p3 = new Vector4f( m, -m, height, 1).mul(rot);
        Vector4f p4 = new Vector4f( m,  m, height, 1).mul(rot);
        Vector4f p5 = new Vector4f(-m, -m, 0,      1).mul(rot);
        Vector4f p6 = new Vector4f(-m,  m, 0,      1).mul(rot);
        Vector4f p7 = new Vector4f( m,  m, 0,      1).mul(rot);
        Vector4f p8 = new Vector4f( m, -m, 0,      1).mul(rot);

        quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a, -1, 0, 0, p6, p1, p2, p5);
        quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a, -1, 0, 0, p8, p3, p4, p7);
        quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a,  0, 1, 0, p7, p4, p1, p6);
        quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a, -1, 0, 0, p5, p2, p3, p8);

        if (tex != topTex) {
            TextureAtlasSprite ts = atlas.apply(topTex);
            u0 = ts.getU0(); u1 = ts.getU1(); v0 = ts.getV0(); v1 = ts.getV1();
        }
        if (top)    quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a, 0, 0, 1, p6, p5, p8, p7);
        if (bottom) quad(vc, pose, light, u0, u1, v0, v1, r, g, b, a, 0, 0, 1, p2, p1, p4, p3);
    }

    private static void quad(VertexConsumer vc, PoseStack pose, int light,
                              float u0, float u1, float v0, float v1,
                              float r, float g, float b, float a,
                              float nx, float ny, float nz,
                              Vector4f q1, Vector4f q2, Vector4f q3, Vector4f q4) {
        var mat = pose.last().pose();
        vc.vertex(mat, q1.x, q1.y, q1.z).color(r, g, b, a).uv(u0, v0).uv2(light).normal(nx, ny, nz).endVertex();
        vc.vertex(mat, q2.x, q2.y, q2.z).color(r, g, b, a).uv(u0, v1).uv2(light).normal(nx, ny, nz).endVertex();
        vc.vertex(mat, q3.x, q3.y, q3.z).color(r, g, b, a).uv(u1, v1).uv2(light).normal(nx, ny, nz).endVertex();
        vc.vertex(mat, q4.x, q4.y, q4.z).color(r, g, b, a).uv(u1, v0).uv2(light).normal(nx, ny, nz).endVertex();
    }
}
