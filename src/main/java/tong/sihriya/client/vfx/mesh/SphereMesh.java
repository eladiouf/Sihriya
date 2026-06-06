package tong.sihriya.client.vfx.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

public class SphereMesh implements ProceduralMesh {
    private final float radius;
    private final int rings;
    private final int segments;
    private List<MeshVertex> cached;

    public SphereMesh(float radius, int rings, int segments) {
        this.radius = radius;
        this.rings = rings;
        this.segments = segments;
    }

    @Override
    public List<MeshVertex> generate() {
        if (cached != null) return cached;
        List<MeshVertex> verts = new ArrayList<>();

        for (int i = 0; i < rings; i++) {
            float phi1 = (float) (i * Math.PI / rings);
            float phi2 = (float) ((i + 1) * Math.PI / rings);
            float sinP1 = (float) Math.sin(phi1);
            float cosP1 = (float) Math.cos(phi1);
            float sinP2 = (float) Math.sin(phi2);
            float cosP2 = (float) Math.cos(phi2);

            for (int j = 0; j < segments; j++) {
                float theta1 = (float) (j * 2 * Math.PI / segments);
                float theta2 = (float) ((j + 1) * 2 * Math.PI / segments);
                float sinT1 = (float) Math.sin(theta1);
                float cosT1 = (float) Math.cos(theta1);
                float sinT2 = (float) Math.sin(theta2);
                float cosT2 = (float) Math.cos(theta2);

                float x1 = radius * sinP1 * cosT1, y1 = radius * cosP1, z1 = radius * sinP1 * sinT1;
                float x2 = radius * sinP1 * cosT2, y2 = radius * cosP1, z2 = radius * sinP1 * sinT2;
                float x3 = radius * sinP2 * cosT2, y3 = radius * cosP2, z3 = radius * sinP2 * sinT2;
                float x4 = radius * sinP2 * cosT1, y4 = radius * cosP2, z4 = radius * sinP2 * sinT1;

                float nr = 1.0f / radius;
                float u1 = (float) j / segments, v1 = (float) i / rings;
                float u2 = (float) (j + 1) / segments, v2 = (float) (i + 1) / rings;

                verts.add(new MeshVertex(x1, y1, z1, x1 * nr, y1 * nr, z1 * nr, u1, v1));
                verts.add(new MeshVertex(x2, y2, z2, x2 * nr, y2 * nr, z2 * nr, u2, v1));
                verts.add(new MeshVertex(x3, y3, z3, x3 * nr, y3 * nr, z3 * nr, u2, v2));
                verts.add(new MeshVertex(x4, y4, z4, x4 * nr, y4 * nr, z4 * nr, u1, v2));
            }
        }
        cached = verts;
        return verts;
    }

    @Override
    public int vertexCount() {
        return cached != null ? cached.size() : generate().size();
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, ResourceLocation texture,
                        float[] color, float alpha, float scale, boolean fade) {
        MeshRenderer.render(stack, buffer, generate(), texture, color, alpha, scale, fade);
    }
}
