package tong.sihriya.client.vfx.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SchoolColors;
import tong.sihriya.vfx.VFXDefinition;
import tong.sihriya.vfx.VFXRegistry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SihriyaVFXData extends SimplePreparableReloadListener<Void> {
    private static final Gson GSON = new Gson();
    private static final String[] SCHOOLS = {
        "fire", "water", "wind", "earth", "lightning",
        "ice", "lava", "necromancy", "lumamancy"
    };

    @Override
    protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
        VFXRegistry.clear();
        for (String school : SCHOOLS) {
            loadSchool(manager, school);
        }
        return null;
    }

    private void loadSchool(ResourceManager manager, String school) {
        ResourceLocation loc = new ResourceLocation("sihriya", "sihriya_vfx/" + school + ".json");
        var opt = manager.getResource(loc);
        if (opt.isEmpty()) {
            registerDefaults(school);
            return;
        }
        try (InputStream is = opt.get().open();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) { registerDefaults(school); return; }

            JsonObject defaults = root.getAsJsonObject("defaults");
            VFXDefinition schoolDef = parseDefinition(school, defaults);
            if (schoolDef != null) VFXRegistry.setSchoolDefault(school, schoolDef);

            JsonObject overrides = root.getAsJsonObject("overrides");
            if (overrides != null) {
                for (var entry : overrides.entrySet()) {
                    String spellId = entry.getKey();
                    JsonObject overrideData = entry.getValue().getAsJsonObject();
                    VFXDefinition overrideDef = parseDefinition(school, overrideData);
                    if (overrideDef != null) VFXRegistry.register(spellId, overrideDef);
                }
            }
        } catch (Exception e) {
            Sihriya.LOGGER.warn("Failed to load VFX data for school {}: {}", school, e.getMessage());
            registerDefaults(school);
        }
    }

    private void registerDefaults(String school) {
        float[] c1 = SchoolColors.get(school);
        float[] c2 = SchoolColors.getSecondary(school);
        VFXDefinition def = new VFXDefinition(null, school,
            new VFXDefinition.ProjectileConfig(1.0f, "sphere", 8, 1.0f, c1, c2, 3.0f, 0.15f, 0.2f,
                new VFXDefinition.EmitterConfig("helix", 0, 0, 0.05f, 0.3f, 0, 0, 0, 0, 0, 3, 10), null, true),
            new VFXDefinition.BeamConfig(0.3f, 8, 6, c1, c2, 1.5f, true, 0.1f, false, 0),
            new VFXDefinition.ImpactConfig(
                new VFXDefinition.EmitterConfig("burst", 20, 0.5f, 0.3f, 0, 0, 0, 0, 0, 0, 0, 0),
                true, 1, 0.5f, 3.0f, 0, false, 0, false, 0, c1, c2),
            null, null, null, null, null, null);
        VFXRegistry.setSchoolDefault(school, def);
    }

    private VFXDefinition parseDefinition(String school, JsonObject data) {
        if (data == null) return null;
        float[] c1 = SchoolColors.get(school);
        float[] c2 = SchoolColors.getSecondary(school);
        VFXDefinition.ProjectileConfig projectile = parseProjectile(data.getAsJsonObject("projectile"), c1, c2);
        VFXDefinition.BeamConfig beam = parseBeam(data.getAsJsonObject("beam"), c1, c2);
        VFXDefinition.ImpactConfig impact = parseImpact(data.getAsJsonObject("impact"), c1, c2);
        return new VFXDefinition(null, school, projectile, beam, impact,
            null, null, null, null, null, null);
    }

    private VFXDefinition.ProjectileConfig parseProjectile(JsonObject obj, float[] c1, float[] c2) {
        if (obj == null) return null;
        float scale = getFloat(obj, "scale", 1.0f);
        String meshType = getString(obj, "meshType", "sphere");
        int meshDetail = getInt(obj, "meshDetail", 8);
        float glowIntensity = getFloat(obj, "glowIntensity", 1.0f);
        float[] color1 = getColor(obj, "color1", c1);
        float[] color2 = getColor(obj, "color2", c2);
        float rotationSpeed = getFloat(obj, "rotationSpeed", 0);
        float pulseSpeed = getFloat(obj, "pulseSpeed", 0);
        float pulseAmount = getFloat(obj, "pulseAmount", 0);
        VFXDefinition.EmitterConfig trail = parseEmitter(obj.getAsJsonObject("trail"));
        VFXDefinition.EmitterConfig aura = parseEmitter(obj.getAsJsonObject("aura"));
        boolean volumetric = getBool(obj, "volumetric", false);
        return new VFXDefinition.ProjectileConfig(scale, meshType, meshDetail, glowIntensity,
            color1, color2, rotationSpeed, pulseSpeed, pulseAmount, trail, aura, volumetric);
    }

    private VFXDefinition.BeamConfig parseBeam(JsonObject obj, float[] c1, float[] c2) {
        if (obj == null) return null;
        float radius = getFloat(obj, "radius", 0.3f);
        int segments = getInt(obj, "segments", 8);
        int rings = getInt(obj, "rings", 6);
        float[] color1 = getColor(obj, "color1", c1);
        float[] color2 = getColor(obj, "color2", c2);
        float glowIntensity = getFloat(obj, "glowIntensity", 1.5f);
        boolean scrolling = getBool(obj, "scrolling", true);
        float scrollSpeed = getFloat(obj, "scrollSpeed", 0.1f);
        boolean noise = getBool(obj, "noise", false);
        float noiseAmount = getFloat(obj, "noiseAmount", 0);
        return new VFXDefinition.BeamConfig(radius, segments, rings, color1, color2,
            glowIntensity, scrolling, scrollSpeed, noise, noiseAmount);
    }

    private VFXDefinition.ImpactConfig parseImpact(JsonObject obj, float[] c1, float[] c2) {
        if (obj == null) return null;
        VFXDefinition.EmitterConfig burst = parseEmitter(obj.getAsJsonObject("burst"));
        boolean shockwave = getBool(obj, "shockwave", true);
        int shockwaveRings = getInt(obj, "shockwaveRings", 1);
        float shockwaveSpeed = getFloat(obj, "shockwaveSpeed", 0.5f);
        float shockwaveRadius = getFloat(obj, "shockwaveRadius", 3.0f);
        int debris = getInt(obj, "debris", 0);
        boolean groundMark = getBool(obj, "groundMark", false);
        int groundMarkDuration = getInt(obj, "groundMarkDuration", 0);
        boolean screenShake = getBool(obj, "screenShake", false);
        float screenShakeIntensity = getFloat(obj, "screenShakeIntensity", 0);
        float[] color1 = getColor(obj, "color1", c1);
        float[] color2 = getColor(obj, "color2", c2);
        return new VFXDefinition.ImpactConfig(burst, shockwave, shockwaveRings, shockwaveSpeed,
            shockwaveRadius, debris, groundMark, groundMarkDuration, screenShake,
            screenShakeIntensity, color1, color2);
    }

    private VFXDefinition.EmitterConfig parseEmitter(JsonObject obj) {
        if (obj == null) return null;
        String type = getString(obj, "type", "burst");
        int count = getInt(obj, "count", 20);
        float speed = getFloat(obj, "speed", 0.5f);
        float spread = getFloat(obj, "spread", 0.3f);
        float radius = getFloat(obj, "radius", 0);
        float startRadius = getFloat(obj, "startRadius", 0);
        float endRadius = getFloat(obj, "endRadius", 0);
        float height = getFloat(obj, "height", 0);
        float angle = getFloat(obj, "angle", 0);
        float contractionSpeed = getFloat(obj, "contractionSpeed", 0);
        int particlesPerTick = getInt(obj, "particlesPerTick", 0);
        int particleLifetime = getInt(obj, "particleLifetime", 0);
        return new VFXDefinition.EmitterConfig(type, count, speed, spread, radius,
            startRadius, endRadius, height, angle, contractionSpeed,
            particlesPerTick, particleLifetime);
    }

    private static float getFloat(JsonObject obj, String key, float def) {
        return obj.has(key) ? obj.get(key).getAsFloat() : def;
    }

    private static int getInt(JsonObject obj, String key, int def) {
        return obj.has(key) ? obj.get(key).getAsInt() : def;
    }

    private static boolean getBool(JsonObject obj, String key, boolean def) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : def;
    }

    private static String getString(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }

    private static float[] getColor(JsonObject obj, String key, float[] def) {
        if (!obj.has(key)) return def;
        var arr = obj.getAsJsonArray(key);
        if (arr == null || arr.size() < 3) return def;
        return new float[]{arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat()};
    }

    @Override
    protected void apply(Void prepared, ResourceManager manager, ProfilerFiller profiler) {
        Sihriya.LOGGER.info("VFX data reload complete");
    }
}
