package tong.sihriya.client.vfx.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterShadersEvent;
import tong.sihriya.Sihriya;

import java.io.IOException;
import java.util.function.Consumer;

public class SihriyaCoreShaders {
    public static final String GLOW_SHADER_NAME = "sihriya_glow";
    private static ShaderInstance glowShader;
    private static AbstractUniform GLOW_TIME;
    private static AbstractUniform GLOW_VIEWPOS;
    private static AbstractUniform GLOW_COLOR;
    private static AbstractUniform GLOW_COLOR2;
    private static AbstractUniform GLOW_INTENSITY;

    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ResourceLocation loc = new ResourceLocation("sihriya", GLOW_SHADER_NAME);
            ShaderInstance instance = new ShaderInstance(
                event.getResourceProvider(), loc, DefaultVertexFormat.NEW_ENTITY
            );
            event.registerShader(instance, (Consumer<ShaderInstance>) shader -> {});
            glowShader = instance;
            GLOW_TIME = instance.safeGetUniform("uTime");
            GLOW_VIEWPOS = instance.safeGetUniform("uViewPos");
            GLOW_COLOR = instance.safeGetUniform("uGlowColor");
            GLOW_COLOR2 = instance.safeGetUniform("uGlowColor2");
            GLOW_INTENSITY = instance.safeGetUniform("uGlowIntensity");
        } catch (IOException e) {
            Sihriya.LOGGER.error("Failed to load glow shader", e);
        }
    }

    public static ShaderInstance getGlowShader() { return glowShader; }

    public static void setGlowUniforms(float time, Vec3 viewPos,
                                        float[] color1, float[] color2, float intensity) {
        if (GLOW_TIME == null) return;
        GLOW_TIME.set(time);
        GLOW_VIEWPOS.set((float)viewPos.x, (float)viewPos.y, (float)viewPos.z);
        GLOW_COLOR.set(color1[0], color1[1], color1[2], 1.0f);
        GLOW_COLOR2.set(color2[0], color2[1], color2[2], 1.0f);
        GLOW_INTENSITY.set(intensity);
    }

    public static void onResourceReload() {
        glowShader = null;
        GLOW_TIME = null;
        GLOW_VIEWPOS = null;
        GLOW_COLOR = null;
        GLOW_COLOR2 = null;
        GLOW_INTENSITY = null;
    }
}
