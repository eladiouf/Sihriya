package tong.sihriya.client.vfx.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

public class ConeMesh implements ProceduralMesh {
    private final float radius;
    private final float height;
    private final int segments;
    private List<MeshVertex> cached;

    public ConeMesh(float radius, float height, int segments) {
        this.radius = radius;
        this.height = height;
        this.segments = segments;
    }

    @Override
    public List<MeshVertex> generate() {
        if (cached != null) return cached;
        List<MeshVertex> verts = new ArrayList<>();

        for (int j = 0; j < segments; j++) {
            float th1 = (float) (j * 2 * Math.PI / segments);
            float th2 = (float) ((j + 1) * 2 * Math.PI / segments);
            float cos1 = (float) Math.cos(th1), sin1 = (float) Math.sin(th1);
            float cos2 = (float) Math.cos(th2), sin2 = (float) Math.sin(th2);

            float bx1 = radius * cos1, bz1 = radius * sin1;
            float bx2 = radius * cos2, bz2 = radius * sin2;

            float nx = (float) Math.cos(th1 + Math.PI / 2);
            float nz = (float) Math.sin(th1 + Math.PI / 2);
            float ny = radius / height;

            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

            verts.add(new MeshVertex(0, height, 0, nx / len, ny / len, nz / len, 0.5f, 1));
            verts.add(new MeshVertex(bx2, 0, bz2, nx / len, ny / len, nz / len, (float) j / segments, 0));
            verts.add(new MeshVertex(bx1, 0, bz1, nx / len, ny / len, nz / len, (float) (j + 1) / segments, 0));

            verts.add(new MeshVertex(0, 0, 0, 0, -1, 0, 0.5f, 0));
            verts.add(new MeshVertex(bx2, 0, bz2, 0, -1, 0, (float) j / segments, 0));
            verts.add(new MeshVertex(bx1, 0, bz1, 0, -1, 0, (float) (j + 1) / segments, 0));
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
