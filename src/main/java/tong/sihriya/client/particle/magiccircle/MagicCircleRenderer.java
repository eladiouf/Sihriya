package tong.sihriya.client.particle.magiccircle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import tong.sihriya.Sihriya;
import tong.sihriya.magiccircle.MagicCircleEntity;

import tong.sihriya.data.SchoolColors;

public class MagicCircleRenderer extends EntityRenderer<MagicCircleEntity> {

    private static ResourceLocation tex(String path) {
        return new ResourceLocation(Sihriya.MODID, "textures/vfx/kenney/" + path + ".png");
    }

    private static final ResourceLocation[] LAYERS = {
        tex("circle_rings_a_streaks"),  // outer — slow runes
        tex("circle_a_streaks"),        // middle — counter symbols
        tex("circle_b_streaks"),        // inner — fast geometry
        tex("magic_05"),                // center — barely moves
    };

    public MagicCircleRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(MagicCircleEntity entity, float entityYaw, float partialTick,
                       PoseStack stack, MultiBufferSource buffer, int packedLight) {
        String school = entity.getSchool();
        float[] rgb = SchoolColors.get(school);

        var anim = entity.getAnimation();
        float alpha = anim.getAlpha();
        if (alpha <= 0.001f) return;

        float s = anim.getRadius() / 3.0f;
        int overlay = OverlayTexture.NO_OVERLAY;

        renderLayer(stack, buffer, rgb, alpha, s, overlay,
            LAYERS[0], anim.getRotationRunes());
        renderLayer(stack, buffer, rgb, alpha, s, overlay,
            LAYERS[1], anim.getRotationSymbols());
        renderLayer(stack, buffer, rgb, alpha, s, overlay,
            LAYERS[2], anim.getRotationGeometry());
        renderLayer(stack, buffer, rgb, alpha, s, overlay,
            LAYERS[3], anim.getRotationCenter());
    }

    private void renderLayer(PoseStack stack, MultiBufferSource buffer,
                             float[] rgb, float alpha, float scale, int overlay,
                             ResourceLocation tex, float rotation) {
        if (tex == null) return;

        stack.pushPose();
        stack.translate(0, 0.02, 0);
        stack.mulPose(Axis.YP.rotationDegrees(rotation));
        stack.scale(scale, 1.0f, scale);

        RenderType renderType = RenderType.entityTranslucent(tex);
        VertexConsumer consumer = buffer.getBuffer(renderType);
        Matrix4f matrix = stack.last().pose();

        consumer.vertex(matrix, -1, 0, -1).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 0).overlayCoords(overlay).uv2(0xF000F0).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, -1, 0,  1).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 1).overlayCoords(overlay).uv2(0xF000F0).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix,  1, 0,  1).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 1).overlayCoords(overlay).uv2(0xF000F0).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix,  1, 0, -1).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 0).overlayCoords(overlay).uv2(0xF000F0).normal(0, 1, 0).endVertex();

        stack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MagicCircleEntity entity) {
        return LAYERS[0];
    }
}
