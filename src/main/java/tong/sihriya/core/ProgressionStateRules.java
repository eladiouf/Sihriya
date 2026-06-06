package tong.sihriya.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ProgressionStateRules {
    private ProgressionStateRules() {
    }

    public record StoredState(
        String activeSchool,
        Map<String, Integer> schoolLevels,
        Map<String, Integer> schoolXp,
        Set<String> unlockedSchools,
        Set<String> learnedSpells
    ) {
    }

    public static StoredState sanitize(
            StoredState state,
            Predicate<String> knownSchool,
            Function<String, String> spellSchool) {
        Map<String, Integer> sanitizedLevels = new HashMap<>();
        Map<String, Integer> sanitizedXp = new HashMap<>();

        for (var entry : state.schoolLevels().entrySet()) {
            String schoolId = entry.getKey();
            if (!knownSchool.test(schoolId)) continue;

            int level = clampLevel(entry.getValue());
            sanitizedLevels.put(schoolId, level);
            sanitizedXp.put(schoolId, clampXp(state.schoolXp().getOrDefault(schoolId, 0), level));
        }

        Set<String> sanitizedUnlocked = new HashSet<>();
        for (String schoolId : state.unlockedSchools()) {
            if (knownSchool.test(schoolId)) {
                sanitizedUnlocked.add(schoolId);
            }
        }

        Set<String> sanitizedSpells = new HashSet<>();
        for (String spellId : state.learnedSpells()) {
            String schoolId = spellSchool.apply(spellId);
            if (schoolId != null && sanitizedUnlocked.contains(schoolId)) {
                sanitizedSpells.add(spellId);
            }
        }

        String active = state.activeSchool();
        if (active == null || !sanitizedUnlocked.contains(active)) {
            active = "";
        }

        return new StoredState(active, sanitizedLevels, sanitizedXp, sanitizedUnlocked, sanitizedSpells);
    }

    public static int clampLevel(int level) {
        return Math.max(0, Math.min(100, level));
    }

    public static int clampXp(int xp, int level) {
        if (xp <= 0 || level >= 100) return 0;
        int nextLevelCost = (level + 1) * (level + 1) * 10;
        return Math.min(xp, nextLevelCost - 1);
    }
}
