package tong.sihriya.client.vfx.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import tong.sihriya.Sihriya;

public class ProceduralTextureHelper {
    private static boolean generated = false;

    // Procédurales
    public static final ResourceLocation BEAM_TEX = new ResourceLocation("sihriya", "vfx/beam");
    public static final ResourceLocation NOISE_TEX = new ResourceLocation("sihriya", "vfx/noise");
    public static final ResourceLocation GLOW_RAMP_TEX = new ResourceLocation("sihriya", "vfx/glow_ramp");
    public static final ResourceLocation LIGHTNING_TEX = new ResourceLocation("sihriya", "vfx/lightning");

    // Kenney Particle Pack (CC0) — chargées directement depuis les assets
    public static final ResourceLocation CIRCLE_RINGS = tex("kenney/circle_rings_a_streaks");
    public static final ResourceLocation CIRCLE_AURA = tex("kenney/circle_c_streaks");
    public static final ResourceLocation SHIELD_MASK = tex("kenney/circle_a_streaks");
    public static final ResourceLocation BEAM_STREAK = tex("kenney/streaks_composed_c");
    public static final ResourceLocation SPARK = tex("kenney/spark_07");
    public static final ResourceLocation MAGIC_GLOW = tex("kenney/magic_05");
    public static final ResourceLocation FLAME = tex("kenney/flame_06");
    public static final ResourceLocation STAR_BURST = tex("kenney/star_06");
    public static final ResourceLocation SMOKE = tex("kenney/smoke_06");
    public static final ResourceLocation FIRE = tex("kenney/fire_01");
    public static final ResourceLocation TWIRL = tex("kenney/twirl_03");
    public static final ResourceLocation TRACE = tex("kenney/trace_04");
    public static final ResourceLocation RING = tex("kenney/ring_b_streaks");

    private static ResourceLocation tex(String path) {
        return new ResourceLocation("sihriya", "textures/vfx/" + path + ".png");
    }

    public static void generate() {
        if (generated) return;
        var texManager = Minecraft.getInstance().getTextureManager();

        texManager.register(BEAM_TEX, new DynamicTexture(createBeamTexture()));
        texManager.register(NOISE_TEX, new DynamicTexture(createNoiseTexture()));
        texManager.register(GLOW_RAMP_TEX, new DynamicTexture(createGlowRampTexture()));
        texManager.register(LIGHTNING_TEX, new DynamicTexture(createLightningTexture()));

        generated = true;
        Sihriya.LOGGER.info("Generated procedural VFX textures");
    }

    private static NativeImage createBeamTexture() {
        NativeImage img = new NativeImage(64, 16, true);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 16; y++) {
                float dx = (x - 32f) / 32f;
                float dy = (y - 8f) / 8f;
                float edge = 1 - dx * dx - dy * dy;
                int alpha = (int) Math.max(0, Math.min(255, edge * 255));
                img.setPixelRGBA(x, y, alpha << 24 | 0x00FFFFFF);
            }
        }
        return img;
    }

    private static NativeImage createNoiseTexture() {
        NativeImage img = new NativeImage(64, 64, true);
        for (int x = 0; x < 64; x++)
            for (int y = 0; y < 64; y++) {
                int val = (int) (Math.random() * 256);
                img.setPixelRGBA(x, y, 255 << 24 | val << 16 | val << 8 | val);
            }
        return img;
    }

    private static NativeImage createGlowRampTexture() {
        NativeImage img = new NativeImage(16, 256, true);
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 256; y++) {
                float t = y / 255f;
                float val = t * t * (3 - 2 * t);
                int v = (int) Math.max(0, Math.min(255, val * 255));
                img.setPixelRGBA(x, 255 - y, 255 << 24 | v << 16 | v << 8 | v);
            }
        return img;
    }

    private static NativeImage createLightningTexture() {
        NativeImage img = new NativeImage(32, 64, true);
        for (int x = 0; x < 32; x++)
            for (int y = 0; y < 64; y++) {
                float center = Math.abs(x - 16) / 16f;
                int alpha = (int) Math.max(0, Math.min(255, (1 - center * center) * 255));
                img.setPixelRGBA(x, y, alpha << 24 | 0x00FFFFFF);
            }
        return img;
    }
}
