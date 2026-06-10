package tong.sihriya.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CircleTextureSampler {
    private static final Map<ResourceLocation, List<Vector2f>> CACHE = new HashMap<>();

    public static List<Vector2f> sample(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, CircleTextureSampler::loadPoints);
    }

    private static List<Vector2f> loadPoints(ResourceLocation tex) {
        List<Vector2f> points = new ArrayList<>();
        var opt = Minecraft.getInstance().getResourceManager().getResource(tex);
        if (opt.isEmpty()) return points;
        try (var image = NativeImage.read(opt.get().open())) {
            int w = image.getWidth();
            int h = image.getHeight();
            int stride = 3;
            for (int y = 0; y < h; y += stride) {
                for (int x = 0; x < w; x += stride) {
                    int rgba = image.getPixelRGBA(x, y);
                    int a = (rgba >> 24) & 0xFF;
                    if (a > 80) {
                        float nx = (x / (float) w) * 2.0f - 1.0f;
                        float ny = (y / (float) h) * 2.0f - 1.0f;
                        points.add(new Vector2f(nx, ny));
                    }
                }
            }
        } catch (Exception e) {
            return points;
        }
        return points;
    }

    private CircleTextureSampler() {}
}
