package tong.sihriya.client.vfx.mesh;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public interface ProceduralMesh {
    List<MeshVertex> generate();
    int vertexCount();
    void render(PoseStack stack, MultiBufferSource buffer, ResourceLocation texture,
                float[] color, float alpha, float scale, boolean fade);
}
