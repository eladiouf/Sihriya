package tong.sihriya.client;

import tong.sihriya.client.gui.SihriyaNotifications;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.sihriya.core.TierUnlockHandler;

import java.util.*;

public class ClientSchoolData {
    private static String activeSchool = "";
    private static Map<String, Integer> schoolLevels = new HashMap<>();
    private static Set<String> unlockedSchools = new HashSet<>();
    private static Set<String> learnedSpells = new HashSet<>();
    public static String lastRequestedSchool = "";
    public static String lastRequestedSpellId = "";
    public static long lastRequestedAtMs = 0;
    public static long lastRequestedCooldownMs = 0;

    // Unified mana — driven by STAT Mod MANA_POOL stat
    public static float mana = 50;
    public static float maxMana = 50;
    public static boolean manaBlocked = false;
    public static long manaBlockRemainingMs = 0;

    public static float getManaPercent() {
        return maxMana > 0 ? Math.min(1f, mana / maxMana) : 0;
    }

    public static boolean isUnlocked(String school) {
        return unlockedSchools.contains(school);
    }

    public static String getActiveSchool() {
        return activeSchool;
    }

    public static int getLevel(String school) {
        return schoolLevels.getOrDefault(school, 0);
    }

    public static boolean isSpellLearned(String spellId) {
        return learnedSpells.contains(spellId);
    }

    public static boolean canCast(SpellData spell) {
        if (!isSpellLearned(spell.id)) return false;
        int tier = spell.tier;
        int level = getLevel(spell.school);
        return TierUnlockHandler.getUnlockLevel(tier) <= level;
    }

    public static Optional<SpellData> getBestCastableSpell(String school) {
        return learnedSpells.stream()
            .map(SpellRegistry::get)
            .filter(Objects::nonNull)
            .filter(spell -> school.equals(spell.school))
            .filter(ClientSchoolData::canCast)
            .sorted((a, b) -> {
                int tier = Integer.compare(b.tier, a.tier);
                return tier != 0 ? tier : a.id.compareTo(b.id);
            })
            .findFirst();
    }

    public static Optional<SpellData> getFirstSchoolSpell(String school) {
        return SpellRegistry.getBySchool(school).stream()
            .sorted(Comparator.comparingInt((SpellData spell) -> spell.tier).thenComparing(spell -> spell.id))
            .findFirst();
    }

    public static void noteCastRequest(String school) {
        lastRequestedSchool = school;
        var spellOpt = getBestCastableSpell(school);
        if (spellOpt.isEmpty()) {
            lastRequestedSpellId = "";
            lastRequestedAtMs = 0;
            lastRequestedCooldownMs = 0;
            return;
        }
        spellOpt.ifPresent(spell -> {
            lastRequestedSpellId = spell.id;
            lastRequestedAtMs = System.currentTimeMillis();
            lastRequestedCooldownMs = Math.max(750L, spell.cooldown * 50L);
        });
    }

    public static void noteServerCastSuccess(String school, String spellId, int cooldownTicks) {
        var spell = SpellRegistry.get(spellId);
        if (spell == null) {
            clearPendingCast();
            return;
        }
        lastRequestedSchool = school;
        lastRequestedSpellId = spellId;
        lastRequestedAtMs = System.currentTimeMillis();
        lastRequestedCooldownMs = Math.max(750L, cooldownTicks * 50L);
    }

    public static void clearPendingCast() {
        lastRequestedSpellId = "";
        lastRequestedAtMs = 0;
        lastRequestedCooldownMs = 0;
    }

    public static Optional<String> getClientCastBlockReason(String school) {
        if (!isUnlocked(school)) return Optional.of("notification.sihriya.cast_locked");
        var spellOpt = getBestCastableSpell(school);
        if (spellOpt.isEmpty()) return Optional.of("notification.sihriya.cast_no_spell");
        if (manaBlocked) return Optional.of("notification.sihriya.cast_mana_locked");
        if (mana < spellOpt.get().manaCost) {
            return Optional.of("notification.sihriya.cast_no_mana");
        }
        return Optional.empty();
    }

    public static Optional<SpellData> getLastRequestedSpell() {
        if (lastRequestedSpellId.isEmpty()) return Optional.empty();
        return Optional.ofNullable(SpellRegistry.get(lastRequestedSpellId));
    }

    public static float getLastRequestedCooldownProgress() {
        if (lastRequestedAtMs <= 0 || lastRequestedCooldownMs <= 0) return 0;
        long elapsed = System.currentTimeMillis() - lastRequestedAtMs;
        return Math.max(0f, Math.min(1f, elapsed / (float) lastRequestedCooldownMs));
    }

    public static boolean hasVisibleCastHud() {
        return getLastRequestedSpell().isPresent() && getLastRequestedCooldownProgress() < 1f;
    }

    public static void applySync(String active, Map<String, Integer> levels,
                                 Set<String> unlocked, Set<String> spells) {
        Set<String> newSchools = new HashSet<>(unlocked);
        newSchools.removeAll(unlockedSchools);
        Set<String> newSpells = new HashSet<>(spells);
        newSpells.removeAll(learnedSpells);

        // Detect tier unlocks before updating levels
        for (var entry : levels.entrySet()) {
            int oldLevel = schoolLevels.getOrDefault(entry.getKey(), 0);
            int newLevel = entry.getValue();
            for (int tier = 1; tier <= 5; tier++) {
                int threshold = TierUnlockHandler.getUnlockLevel(tier);
                if (oldLevel < threshold && newLevel >= threshold) {
                    SihriyaNotifications.tierUnlocked(entry.getKey(), tier);
                }
            }
        }

        activeSchool = active;
        schoolLevels = new HashMap<>(levels);
        unlockedSchools = new HashSet<>(unlocked);
        learnedSpells = new HashSet<>(spells);

        for (String school : newSchools) {
            SihriyaNotifications.schoolUnlocked(school);
        }
        for (String spellId : newSpells) {
            SihriyaNotifications.spellLearned(spellId);
        }
    }
}
