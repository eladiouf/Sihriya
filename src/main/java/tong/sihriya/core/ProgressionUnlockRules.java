package tong.sihriya.core;

import tong.sihriya.data.SchoolRegistry.SchoolData;
import tong.sihriya.data.SpellRegistry.SpellData;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

public final class ProgressionUnlockRules {
    private static final int[] TIER_THRESHOLDS = {0, 25, 50, 75, 100};

    private ProgressionUnlockRules() {}

    public static int unlockLevelForTier(int tier) {
        if (tier < 1 || tier > 5) return 999;
        return TIER_THRESHOLDS[tier - 1];
    }

    public static Set<String> newlyUnlockedSpellIds(
            Collection<SpellData> spells,
            ToIntFunction<String> schoolLevel,
            Set<String> learnedSpellIds) {
        var unlocks = new LinkedHashSet<String>();
        for (var spell : spells) {
            if (learnedSpellIds.contains(spell.id)) continue;
            if (schoolLevel.applyAsInt(spell.school) >= unlockLevelForTier(spell.tier)) {
                unlocks.add(spell.id);
            }
        }
        return unlocks;
    }

    public static Set<String> newlyUnlockedSchoolIds(
            Collection<SchoolData> schools,
            ToIntFunction<String> schoolLevel,
            Set<String> unlockedSchoolIds) {
        var unlocks = new LinkedHashSet<String>();
        for (var school : schools) {
            if (school.unlock == null) continue;
            if (unlockedSchoolIds.contains(school.id)) continue;
            if (isUnlockMet(school.unlock.type, school.unlock.schoolIds(), school.unlock.levels(), schoolLevel)) {
                unlocks.add(school.id);
            }
        }
        return unlocks;
    }

    private static boolean isUnlockMet(
            String type,
            String[] schoolIds,
            int[] levels,
            ToIntFunction<String> schoolLevel) {
        if ("or".equals(type)) {
            for (int i = 0; i < schoolIds.length; i++) {
                if (schoolLevel.applyAsInt(schoolIds[i]) >= levels[i]) return true;
            }
            return false;
        }

        for (int i = 0; i < schoolIds.length; i++) {
            if (schoolLevel.applyAsInt(schoolIds[i]) < levels[i]) return false;
        }
        return true;
    }
}
