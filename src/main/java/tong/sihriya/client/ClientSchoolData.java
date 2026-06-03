package tong.sihriya.client;

import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.sihriya.core.TierUnlockHandler;

import java.util.*;

public class ClientSchoolData {
    public static String activeSchool = "";
    public static Map<String, Integer> schoolLevels = new HashMap<>();
    public static Set<String> unlockedSchools = new HashSet<>();
    public static Set<String> learnedSpells = new HashSet<>();

    public static boolean isUnlocked(String school) {
        return unlockedSchools.contains(school);
    }

    public static int getLevel(String school) {
        return schoolLevels.getOrDefault(school, 0);
    }

    public static boolean canCast(SpellData spell) {
        if (!learnedSpells.contains(spell.id)) return false;
        int tier = spell.tier;
        int level = getLevel(spell.school);
        return TierUnlockHandler.getUnlockLevel(tier) <= level;
    }
}
