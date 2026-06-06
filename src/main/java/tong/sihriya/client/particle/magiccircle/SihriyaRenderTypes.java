package tong.sihriya.client.particle.magiccircle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class SihriyaRenderTypes {
    private static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY =
        new RenderStateShard.TransparencyStateShard("sihriya_additive_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                                       com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
        );

    private static final class ShardAccessor extends RenderStateShard {
        private ShardAccessor() { super("accessor", () -> {}, () -> {}); }
        static final RenderStateShard.ShaderStateShard TRANSLUCENT_SHADER = RENDERTYPE_ENTITY_TRANSLUCENT_SHADER;
    }

    private static final Function<ResourceLocation, RenderType> MAGIC_CIRCLE_GLOW =
        Util.memoize((Function<ResourceLocation, RenderType>) (texture) -> {
            RenderStateShard.TextureStateShard tex = new RenderStateShard.TextureStateShard(texture, false, false);
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(ShardAccessor.TRANSLUCENT_SHADER)
                .setTextureState(tex)
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .createCompositeState(true);
            return RenderType.create("sihriya_magic_circle_glow",
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, false, true, state);
        });

    public static RenderType magicCircleGlow(ResourceLocation texture) {
        return MAGIC_CIRCLE_GLOW.apply(texture);
    }
}
