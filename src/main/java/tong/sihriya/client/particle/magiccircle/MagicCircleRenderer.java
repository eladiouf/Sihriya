package tong.sihriya.client.particle.magiccircle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
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
        tex("circle_rings_a_streaks"),
        tex("circle_a_streaks"),
        tex("circle_b_streaks"),
        tex("circle_05"),
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

        float scale = anim.getRadius() / 3.0f;
        int overlay = OverlayTexture.NO_OVERLAY;
        int fullBright = LightTexture.FULL_BRIGHT;

        stack.pushPose();
        stack.translate(0, 0.5, 0);

        // Billboard vertical : toujours face au joueur
        var camera = entityRenderDispatcher.camera;
        stack.mulPose(Axis.YP.rotationDegrees(180 - camera.getYRot()));
        stack.mulPose(Axis.XP.rotationDegrees(-camera.getXRot()));

        stack.scale(scale, scale, scale);

        renderLayer(stack, buffer, rgb, alpha, overlay, fullBright,
            LAYERS[0], anim.getRotationRunes());
        renderLayer(stack, buffer, rgb, alpha, overlay, fullBright,
            LAYERS[1], anim.getRotationSymbols());
        renderLayer(stack, buffer, rgb, alpha, overlay, fullBright,
            LAYERS[2], anim.getRotationGeometry());
        renderLayer(stack, buffer, rgb, alpha, overlay, fullBright,
            LAYERS[3], anim.getRotationCenter());

        stack.popPose();
    }

    private void renderLayer(PoseStack stack, MultiBufferSource buffer,
                             float[] rgb, float alpha, int overlay, int packedLight,
                             ResourceLocation texture, float rotation) {
        if (texture == null) return;

        stack.pushPose();
        stack.mulPose(Axis.ZP.rotationDegrees(rotation));
        stack.mulPose(Axis.YP.rotationDegrees(rotation * 0.5f));

        RenderType renderType;
        try {
            renderType = SihriyaRenderTypes.magicCircleGlow(texture);
        } catch (Exception e) {
            renderType = RenderType.entityTranslucent(texture);
        }
        VertexConsumer consumer = buffer.getBuffer(renderType);
        Matrix4f matrix = stack.last().pose();

        consumer.vertex(matrix, -0.5f, -0.5f, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 0).overlayCoords(overlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, -0.5f,  0.5f, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(0, 1).overlayCoords(overlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix,  0.5f,  0.5f, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 1).overlayCoords(overlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix,  0.5f, -0.5f, 0).color(rgb[0], rgb[1], rgb[2], alpha).uv(1, 0).overlayCoords(overlay).uv2(packedLight).normal(0, 1, 0).endVertex();

        stack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MagicCircleEntity entity) {
        return LAYERS[0];
    }
}
