package tong.sihriya.vfx;

import java.util.HashMap;
import java.util.Map;

public class VFXRegistry {
    private static final Map<String, VFXDefinition> spellOverrides = new HashMap<>();
    private static final Map<String, VFXDefinition> schoolDefaults = new HashMap<>();
    private static VFXDefinition globalDefaults;

    public static VFXDefinition get(String spellId, String schoolId) {
        VFXDefinition def = spellOverrides.get(spellId);
        if (def != null) return def;
        def = schoolDefaults.get(schoolId);
        if (def != null) return def;
        return globalDefaults;
    }

    public static void register(String spellId, VFXDefinition def) {
        spellOverrides.put(spellId, def);
    }

    public static void setSchoolDefault(String schoolId, VFXDefinition def) {
        schoolDefaults.put(schoolId, def);
    }

    public static void setGlobalDefaults(VFXDefinition def) {
        globalDefaults = def;
    }

    public static void clear() {
        spellOverrides.clear();
        schoolDefaults.clear();
        globalDefaults = null;
    }
}
