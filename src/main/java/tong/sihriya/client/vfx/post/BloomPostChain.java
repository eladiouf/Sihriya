package tong.sihriya.client.vfx.post;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import tong.sihriya.Sihriya;

import java.io.IOException;

public class BloomPostChain {
    private static BloomPostChain INSTANCE;
    private PostChain postChain;
    private boolean active;

    public static void init(Minecraft mc) {
        INSTANCE = new BloomPostChain(mc);
    }

    private BloomPostChain(Minecraft mc) {
        try {
            postChain = new PostChain(
                mc.getTextureManager(), mc.getResourceManager(),
                mc.getMainRenderTarget(),
                new ResourceLocation("sihriya:shaders/post/bloom.json")
            );
            postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            active = true;
        } catch (IOException e) {
            Sihriya.LOGGER.warn("Failed to load bloom post-chain", e);
            active = false;
        }
    }

    public static boolean isActive() { return INSTANCE != null && INSTANCE.active; }
    public static void setActive(boolean v) { if (INSTANCE != null) INSTANCE.active = v; }

    public static void processFrame(float partialTick) {
        if (isActive() && INSTANCE.postChain != null)
            INSTANCE.postChain.process(partialTick);
    }

    public static void onResize(int w, int h) {
        if (INSTANCE != null && INSTANCE.postChain != null)
            INSTANCE.postChain.resize(w, h);
    }
}
