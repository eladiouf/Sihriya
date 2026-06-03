package tong.sihriya.client.particle.magiccircle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import tong.sihriya.Sihriya;

import java.util.Map;

public class MagicCircleRenderer extends EntityRenderer<MagicCircleEntity> {
    private static final Map<String, float[]> SCHOOL_COLORS = Map.of(
        "fire",       new float[]{1.0f, 0.4f, 0.1f},
        "water",      new float[]{0.2f, 0.6f, 1.0f},
        "wind",       new float[]{0.9f, 0.9f, 1.0f},
        "earth",      new float[]{0.3f, 0.7f, 0.2f},
        "lightning",  new float[]{1.0f, 0.9f, 0.1f},
        "ice",        new float[]{0.5f, 0.8f, 1.0f},
        "lava",       new float[]{1.0f, 0.2f, 0.0f},
        "necromancy", new float[]{0.5f, 0.0f, 0.8f},
        "lumamancy",  new float[]{1.0f, 0.85f, 0.4f}
    );

    private static ResourceLocation tex(String school, int layer) {
        String prefix = "textures/magiccircle/" + school + "_";
        // lumamancy uses lumagie files
        String base = school.equals("lumamancy") ? "lumagie" : school;
        return ResourceLocation.fromNamespaceAndPath(Sihriya.MODID,
            "textures/magiccircle/" + base + "_" + layer + ".png");
    }

    public MagicCircleRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(MagicCircleEntity entity, float entityYaw, float partialTick,
                       PoseStack stack, MultiBufferSource buffer, int packedLight) {
        String school = entity.getSchool();
        float[] rgb = SCHOOL_COLORS.getOrDefault(school, new float[]{1, 1, 1});

        var anim = entity.getAnimation();
        float alpha = anim.getAlpha();
        if (alpha <= 0.001f) return;

        float s = anim.getRadius() / 3.0f;

        renderLayer(stack, buffer, rgb, alpha, s,
            tex(school, 0), anim.getRotationRunes());
        renderLayer(stack, buffer, rgb, alpha, s,
            tex(school, 1), anim.getRotationSymbols());
        renderLayer(stack, buffer, rgb, alpha, s,
            tex(school, 2), anim.getRotationGeometry());
        renderLayer(stack, buffer, rgb, alpha, s,
            tex(school, 3), anim.getRotationCenter());
    }

    private void renderLayer(PoseStack stack, MultiBufferSource buffer,
                             float[] rgb, float alpha, float scale,
                             ResourceLocation tex, float rotation) {
        if (tex == null) return;

        stack.pushPose();
        stack.translate(0, 0.02, 0);
        stack.mulPose(Axis.YP.rotationDegrees(rotation));
        stack.scale(scale, 1.0f, scale);

        RenderType renderType = SihriyaRenderTypes.magicCircleGlow(tex);
        VertexConsumer consumer = buffer.getBuffer(renderType);
        Matrix4f matrix = stack.last().pose();

        consumer.vertex(matrix, -1, 0, -1).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 0).endVertex();
        consumer.vertex(matrix, -1, 0,  1).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 1).endVertex();
        consumer.vertex(matrix,  1, 0,  1).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 1).endVertex();
        consumer.vertex(matrix,  1, 0, -1).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 0).endVertex();

        stack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MagicCircleEntity entity) {
        return tex(entity.getSchool(), 0);
    }
}
