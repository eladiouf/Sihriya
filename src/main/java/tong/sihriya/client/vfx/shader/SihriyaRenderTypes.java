package tong.sihriya.client.vfx.shader;

import com.mojang.blaze3d.platform.GlStateManager;
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
        new RenderStateShard.TransparencyStateShard("additive_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
        );

    private static final RenderStateShard.OverlayStateShard NO_OVERLAY =
        new RenderStateShard.OverlayStateShard(true);

    private static final Function<ResourceLocation, RenderType> GLOW_MESH =
        Util.memoize(tex -> RenderType.create(
            "sihriya_glow_mesh",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            512, false, false,
            RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(SihriyaCoreShaders::getGlowShader))
                .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setCullState(new RenderStateShard.CullStateShard(false))
                .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                .setOverlayState(NO_OVERLAY)
                .setDepthTestState(new RenderStateShard.DepthTestStateShard("lequal", 515))
                .createCompositeState(true)
        ));

    public static RenderType glowMesh(ResourceLocation texture) { return GLOW_MESH.apply(texture); }
}
