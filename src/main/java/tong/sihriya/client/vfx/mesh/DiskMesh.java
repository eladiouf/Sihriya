package tong.sihriya.client.vfx.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

public class DiskMesh implements ProceduralMesh {
    private final float radius;
    private final int rings;
    private final int segments;
    private List<MeshVertex> cached;

    public DiskMesh(float radius, int rings, int segments) {
        this.radius = radius;
        this.rings = rings;
        this.segments = segments;
    }

    @Override
    public List<MeshVertex> generate() {
        if (cached != null) return cached;
        List<MeshVertex> verts = new ArrayList<>();

        for (int i = 0; i < rings; i++) {
            float r1 = radius * (float) i / rings;
            float r2 = radius * (float) (i + 1) / rings;

            for (int j = 0; j < segments; j++) {
                float th1 = (float) (j * 2 * Math.PI / segments);
                float th2 = (float) ((j + 1) * 2 * Math.PI / segments);
                float cos1 = (float) Math.cos(th1), sin1 = (float) Math.sin(th1);
                float cos2 = (float) Math.cos(th2), sin2 = (float) Math.sin(th2);

                verts.add(new MeshVertex(r1 * cos1, 0, r1 * sin1, 0, 1, 0, (float) j / segments, (float) i / rings));
                verts.add(new MeshVertex(r2 * cos1, 0, r2 * sin1, 0, 1, 0, (float) j / segments, (float) (i + 1) / rings));
                verts.add(new MeshVertex(r2 * cos2, 0, r2 * sin2, 0, 1, 0, (float) (j + 1) / segments, (float) (i + 1) / rings));
                verts.add(new MeshVertex(r1 * cos2, 0, r1 * sin2, 0, 1, 0, (float) (j + 1) / segments, (float) i / rings));
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
