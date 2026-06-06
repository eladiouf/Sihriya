package tong.sihriya.animation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import tong.sihriya.Sihriya;

import java.util.HashMap;
import java.util.Map;

public class SpellAnimationLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<String, SpellAnimSet> REGISTRY = new HashMap<>();

    public SpellAnimationLoader() {
        super(GSON, "sihriya_spell_animations");
    }

    public record SpellAnimSet(
        String chant,
        String cast,
        String continuous,
        String staffChant,
        String staffCast
    ) {
        public static SpellAnimSet EMPTY = new SpellAnimSet(null, null, null, null, null);

        public boolean isEmpty() {
            return chant == null && cast == null && continuous == null
                && staffChant == null && staffCast == null;
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList,
                         ResourceManager resourceManager, ProfilerFiller profiler) {
        REGISTRY.clear();
        int[] count = {0};

        resourceList.forEach((location, json) -> {
            JsonObject root = json.getAsJsonObject();
            if (!root.has("spells")) return;

            JsonObject spells = root.getAsJsonObject("spells");
            spells.entrySet().forEach(entry -> {
                String spellName = entry.getKey();
                JsonObject data = entry.getValue().getAsJsonObject();
                try {
                    SpellAnimSet set = new SpellAnimSet(
                        getStringOrNull(data, "chant_animation"),
                        getStringOrNull(data, "cast_animation"),
                        getStringOrNull(data, "continuous_animation"),
                        getStringOrNull(data, "staff_chant"),
                        getStringOrNull(data, "staff_cast")
                    );
                    REGISTRY.put(spellName, set);
                    count[0]++;
                } catch (Exception e) {
                    Sihriya.LOGGER.warn("Failed to load anim for spell '{}': {}", spellName, e.getMessage());
                }
            });
        });
        Sihriya.LOGGER.info("SpellAnimationLoader loaded {} spell animation mappings from {}", count[0], resourceList.keySet());
    }

    public static String getAnimation(String spellId, SihriyaAnimationPlayer.SpellPhase phase) {
        // 1. Per-spell override
        SpellAnimSet set = REGISTRY.get(spellId);
        if (set != null && !set.isEmpty()) {
            return getByPhase(set, phase);
        }

        // 2. School-based (__school__ pattern: "fire.fireball" -> "__fire__")
        int dot = spellId.indexOf('.');
        if (dot > 0) {
            String school = spellId.substring(0, dot);
            String schoolKey = "__" + school + "__";
            SpellAnimSet schoolSet = REGISTRY.get(schoolKey);
            if (schoolSet != null && !schoolSet.isEmpty()) {
                return getByPhase(schoolSet, phase);
            }
        }

        // 3. Fallback default
        set = REGISTRY.get("default");
        if (set == null || set.isEmpty()) return null;
        return getByPhase(set, phase);
    }

    private static String getByPhase(SpellAnimSet set, SihriyaAnimationPlayer.SpellPhase phase) {
        return switch (phase) {
            case CHANT -> set.chant();
            case CAST -> set.cast();
            case CONTINUOUS -> set.continuous();
        };
    }

    private static String getStringOrNull(JsonObject obj, String key) {
        if (!obj.has(key)) return null;
        String val = obj.get(key).getAsString();
        return val.isEmpty() ? null : val;
    }
}
