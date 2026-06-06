package tong.sihriya.client.vfx.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

public class TorusMesh implements ProceduralMesh {
    private final float majorRadius;
    private final float minorRadius;
    private final int segments;
    private final int rings;
    private List<MeshVertex> cached;

    public TorusMesh(float majorRadius, float minorRadius, int segments, int rings) {
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
        this.segments = segments;
        this.rings = rings;
    }

    @Override
    public List<MeshVertex> generate() {
        if (cached != null) return cached;
        List<MeshVertex> verts = new ArrayList<>();

        for (int i = 0; i < rings; i++) {
            float u1 = (float) i / rings;
            float u2 = (float) (i + 1) / rings;
            float phi1 = (float) (i * 2 * Math.PI / rings);
            float phi2 = (float) ((i + 1) * 2 * Math.PI / rings);

            for (int j = 0; j < segments; j++) {
                float v1 = (float) j / segments;
                float v2 = (float) (j + 1) / segments;
                float th1 = (float) (j * 2 * Math.PI / segments);
                float th2 = (float) ((j + 1) * 2 * Math.PI / segments);

                float cosP1 = (float) Math.cos(phi1), sinP1 = (float) Math.sin(phi1);
                float cosP2 = (float) Math.cos(phi2), sinP2 = (float) Math.sin(phi2);
                float cosT1 = (float) Math.cos(th1), sinT1 = (float) Math.sin(th1);
                float cosT2 = (float) Math.cos(th2), sinT2 = (float) Math.sin(th2);

                float r1 = majorRadius + minorRadius * cosT1;
                float r2 = majorRadius + minorRadius * cosT2;

                float x1 = r1 * cosP1, y1 = minorRadius * sinT1, z1 = r1 * sinP1;
                float x2 = r2 * cosP1, y2 = minorRadius * sinT2, z2 = r2 * sinP1;
                float x3 = r2 * cosP2, y3 = minorRadius * sinT2, z3 = r2 * sinP2;
                float x4 = r1 * cosP2, y4 = minorRadius * sinT1, z4 = r1 * sinP2;

                float nx1 = cosT1 * cosP1, ny1 = sinT1, nz1 = cosT1 * sinP1;
                float nx2 = cosT2 * cosP1, ny2 = sinT2, nz2 = cosT2 * sinP1;
                float nx3 = cosT2 * cosP2, ny3 = sinT2, nz3 = cosT2 * sinP2;
                float nx4 = cosT1 * cosP2, ny4 = sinT1, nz4 = cosT1 * sinP2;

                verts.add(new MeshVertex(x1, y1, z1, nx1, ny1, nz1, v1, u1));
                verts.add(new MeshVertex(x2, y2, z2, nx2, ny2, nz2, v2, u1));
                verts.add(new MeshVertex(x3, y3, z3, nx3, ny3, nz3, v2, u2));
                verts.add(new MeshVertex(x4, y4, z4, nx4, ny4, nz4, v1, u2));
            }
        }
        cached = verts;
        return verts;
    }

    @Override
    public int vertexCount() { return cached != null ? cached.size() : generate().size(); }

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, ResourceLocation texture,
                        float[] color, float alpha, float scale, boolean fade) {
        MeshRenderer.render(stack, buffer, generate(), texture, color, alpha, scale, fade);
    }
}
