package tong.sihriya.client.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SchoolColors;
import tong.sihriya.projectile.SpellProjectile;

public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Sihriya.MODID, "textures/particle/glow_spark.png");

    public SpellProjectileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(SpellProjectile entity, float yaw, float partialTick, PoseStack stack,
                       MultiBufferSource buffer, int light) {
        Camera camera = this.entityRenderDispatcher.camera;
        Quaternionf rotation = camera.rotation();
        float scale = 0.6f + (entity.tickCount % 10) * 0.03f;
        float[] color = SchoolColors.get(entity.getSchoolId());

        stack.pushPose();
        stack.translate(0, 0.15, 0);
        stack.scale(scale, scale, scale);
        stack.mulPose(rotation);

        RenderType renderType = RenderType.entityTranslucentEmissive(TEXTURE);
        VertexConsumer consumer = buffer.getBuffer(renderType);
        Matrix4f matrix = stack.last().pose();
        int overlay = OverlayTexture.NO_OVERLAY;

        consumer.vertex(matrix, -1, -1, 0).color(color[0], color[1], color[2], 1f).uv(0, 0).overlayCoords(overlay).uv2(0xF000F0).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, -1, 1, 0).color(color[0], color[1], color[2], 1f).uv(0, 1).overlayCoords(overlay).uv2(0xF000F0).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, 1, 1, 0).color(color[0], color[1], color[2], 1f).uv(1, 1).overlayCoords(overlay).uv2(0xF000F0).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, 1, -1, 0).color(color[0], color[1], color[2], 1f).uv(1, 0).overlayCoords(overlay).uv2(0xF000F0).normal(0, 1, 0).endVertex();

        stack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SpellProjectile entity) {
        return TEXTURE;
    }
}
