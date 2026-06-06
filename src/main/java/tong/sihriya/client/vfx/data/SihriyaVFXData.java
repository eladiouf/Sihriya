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
        return new VFXDefinition(null, school,
            new VFXDefinition.ProjectileConfig(1.0f, "sphere", 8, 1.0f, c1, c2, 3.0f, 0.15f, 0.2f, null, null, true),
            new VFXDefinition.BeamConfig(0.3f, 8, 6, c1, c2, 1.5f, true, 0.1f, false, 0),
            new VFXDefinition.ImpactConfig(null, true, 1, 0.5f, 3.0f, 0, false, 0, false, 0, c1, c2),
            null, null, null, null, null, null);
    }

    @Override
    protected void apply(Void prepared, ResourceManager manager, ProfilerFiller profiler) {
        Sihriya.LOGGER.info("VFX data reload complete");
    }
}
