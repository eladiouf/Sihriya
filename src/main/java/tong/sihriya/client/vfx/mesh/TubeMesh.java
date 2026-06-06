package tong.sihriya.client.vfx.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public class TubeMesh implements ProceduralMesh {
    private final Vec3 start;
    private final Vec3 end;
    private final float radius;
    private final int segments;
    private final int rings;
    private List<MeshVertex> cached;

    public TubeMesh(Vec3 start, Vec3 end, float radius, int segments, int rings) {
        this.start = start;
        this.end = end;
        this.radius = radius;
        this.segments = segments;
        this.rings = rings;
    }

    @Override
    public List<MeshVertex> generate() {
        if (cached != null) return cached;
        List<MeshVertex> verts = new ArrayList<>();

        Vec3 dir = end.subtract(start).normalize();
        Vec3 up = Math.abs(dir.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 right = dir.cross(up).normalize();
        Vec3 fwd = right.cross(dir).normalize();

        for (int i = 0; i < rings - 1; i++) {
            float t0 = (float) i / (rings - 1);
            float t1 = (float) (i + 1) / (rings - 1);
            Vec3 c0 = start.add(end.subtract(start).scale(t0));
            Vec3 c1 = start.add(end.subtract(start).scale(t1));
            float r0 = radius * (1 - 4 * (t0 - 0.5f) * (t0 - 0.5f));
            float r1 = radius * (1 - 4 * (t1 - 0.5f) * (t1 - 0.5f));

            for (int j = 0; j < segments; j++) {
                float th1 = (float) (j * 2 * Math.PI / segments);
                float th2 = (float) ((j + 1) * 2 * Math.PI / segments);
                float cos1 = (float) Math.cos(th1), sin1 = (float) Math.sin(th1);
                float cos2 = (float) Math.cos(th2), sin2 = (float) Math.sin(th2);

                Vec3 p00 = c0.add(right.scale(r0 * cos1)).add(fwd.scale(r0 * sin1));
                Vec3 p01 = c0.add(right.scale(r0 * cos2)).add(fwd.scale(r0 * sin2));
                Vec3 p10 = c1.add(right.scale(r1 * cos1)).add(fwd.scale(r1 * sin1));
                Vec3 p11 = c1.add(right.scale(r1 * cos2)).add(fwd.scale(r1 * sin2));

                Vec3 n00 = p00.subtract(c0).normalize();
                Vec3 n01 = p01.subtract(c0).normalize();
                Vec3 n10 = p10.subtract(c1).normalize();
                Vec3 n11 = p11.subtract(c1).normalize();

                float u1 = (float) j / segments, u2 = (float) (j + 1) / segments;

                verts.add(new MeshVertex(p00, n00, u1, t0));
                verts.add(new MeshVertex(p01, n01, u2, t0));
                verts.add(new MeshVertex(p11, n11, u2, t1));
                verts.add(new MeshVertex(p10, n10, u1, t1));
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
        stack.pushPose();
        MeshRenderer.render(stack, buffer, generate(), texture, color, alpha, scale, fade);
        stack.popPose();
    }
}
