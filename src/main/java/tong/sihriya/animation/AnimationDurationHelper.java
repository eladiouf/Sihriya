package tong.sihriya.animation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import tong.sihriya.Sihriya;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class AnimationDurationHelper {
    private static final Gson GSON = new Gson();
    private static final Map<String, Float> DURATION_CACHE = new HashMap<>();
    private static boolean loaded = false;

    public static boolean isLoaded() { return loaded; }

    public static void load(ResourceManager manager) {
        DURATION_CACHE.clear();
        int count = 0;
        for (var entry : SihriyaAnimations.getAllPaths().entrySet()) {
            String animName = entry.getKey();
            String relativePath = entry.getValue();
            ResourceLocation loc = new ResourceLocation("sihriya", relativePath);
            try {
                var opt = manager.getResource(loc);
                if (opt.isEmpty()) continue;
                float maxTime = parseMaxTime(opt.get());
                DURATION_CACHE.put(animName, maxTime > 0 ? maxTime : 0.5f);
                count++;
            } catch (Exception e) {
                DURATION_CACHE.put(animName, 0.5f);
            }
        }
        loaded = true;
        Sihriya.LOGGER.info("Loaded {} animation durations", count);
    }

    public static void load(MinecraftServer server) {
        load(server.getResourceManager());
    }

    private static float parseMaxTime(net.minecraft.server.packs.resources.Resource resource) throws Exception {
        JsonObject root = GSON.fromJson(new InputStreamReader(resource.open()), JsonObject.class);
        float maxTime = 0;

        if (root.has("joints")) {
            var joints = root.getAsJsonArray("joints");
            for (var joint : joints) {
                if (joint.isJsonObject()) {
                    var obj = joint.getAsJsonObject();
                    if (obj.has("positions")) maxTime = Math.max(maxTime, maxFromTimeArray(obj, "positions"));
                    if (obj.has("rotations")) maxTime = Math.max(maxTime, maxFromTimeArray(obj, "rotations"));
                }
            }
            return maxTime;
        }

        for (var key : root.keySet()) {
            var elem = root.get(key);
            if (elem.isJsonObject()) {
                var obj = elem.getAsJsonObject();
                if (obj.has("time")) maxTime = Math.max(maxTime, maxFromTimeArray(obj, "time"));
            }
        }
        return maxTime;
    }

    private static float maxFromTimeArray(JsonObject obj, String field) {
        float max = 0;
        if (obj.has(field)) {
            var arr = obj.get(field).getAsJsonArray();
            for (var val : arr) {
                float t = val.getAsFloat();
                if (t > max) max = t;
            }
        }
        return max;
    }

    public static float getDuration(String animName) {
        return DURATION_CACHE.getOrDefault(animName, 0.5f);
    }
}
