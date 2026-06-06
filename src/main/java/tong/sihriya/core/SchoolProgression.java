package tong.sihriya.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraftforge.common.util.INBTSerializable;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.data.SpellRegistry;

import java.util.*;

public class SchoolProgression implements INBTSerializable<CompoundTag> {
    private final Map<String, Integer> schoolLevels = new HashMap<>();
    private final Map<String, Integer> schoolXp = new HashMap<>();
    private final Set<String> unlockedSchools = new HashSet<>();
    private final Set<String> learnedSpells = new HashSet<>();
    private String activeSchool = "";

    public int getLevel(String schoolId) { return schoolLevels.getOrDefault(schoolId, 0); }
    public int getXp(String schoolId) { return schoolXp.getOrDefault(schoolId, 0); }
    public boolean isSchoolUnlocked(String schoolId) { return unlockedSchools.contains(schoolId); }
    public boolean isSpellLearned(String spellId) { return learnedSpells.contains(spellId); }
    public String getActiveSchool() { return activeSchool; }
    public void setActiveSchool(String schoolId) { this.activeSchool = schoolId; }

    public void unlockSchool(String schoolId) { unlockedSchools.add(schoolId); }
    public void learnSpell(String spellId) { learnedSpells.add(spellId); }

    public void addXp(String schoolId, int amount) {
        int xp = schoolXp.getOrDefault(schoolId, 0) + amount;
        int level = schoolLevels.getOrDefault(schoolId, 0);
        while (level < 100) {
            int needed = (level + 1) * (level + 1) * 10;
            if (xp < needed) break;
            xp -= needed;
            level++;
            schoolLevels.put(schoolId, level);
        }
        schoolXp.put(schoolId, xp);
    }

    public Set<String> getLearnedSpells() { return Set.copyOf(learnedSpells); }
    public Set<String> getUnlockedSchools() { return Set.copyOf(unlockedSchools); }

    /** Retourne l'ID de l'école avec le niveau le plus élevé (pour le premier join). */
    public String getHighestSchool() {
        return schoolLevels.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("");
    }

    public List<String> getSpellsForSchool(String schoolId) {
        return learnedSpells.stream()
            .filter(id -> id.startsWith(schoolId + "."))
            .toList();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ActiveSchool", activeSchool);
        // Levels
        ListTag lvlList = new ListTag();
        for (var e : schoolLevels.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("School", e.getKey());
            entry.putInt("Level", e.getValue());
            entry.putInt("XP", schoolXp.getOrDefault(e.getKey(), 0));
            lvlList.add(entry);
        }
        tag.put("SchoolLevels", lvlList);
        // Unlocked schools
        ListTag unlockedList = new ListTag();
        for (String s : unlockedSchools) unlockedList.add(StringTag.valueOf(s));
        tag.put("UnlockedSchools", unlockedList);
        // Learned spells
        ListTag spellsList = new ListTag();
        for (String s : learnedSpells) spellsList.add(StringTag.valueOf(s));
        tag.put("LearnedSpells", spellsList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        Map<String, Integer> loadedLevels = new HashMap<>();
        Map<String, Integer> loadedXp = new HashMap<>();
        ListTag lvlList = tag.getList("SchoolLevels", 10);
        for (int i = 0; i < lvlList.size(); i++) {
            CompoundTag entry = lvlList.getCompound(i);
            String schoolId = entry.getString("School");
            loadedLevels.put(schoolId, entry.getInt("Level"));
            loadedXp.put(schoolId, entry.getInt("XP"));
        }
        Set<String> loadedUnlocked = new HashSet<>();
        ListTag unlockedList = tag.getList("UnlockedSchools", 8);
        for (int i = 0; i < unlockedList.size(); i++) loadedUnlocked.add(unlockedList.getString(i));
        Set<String> loadedSpells = new HashSet<>();
        ListTag spellsList = tag.getList("LearnedSpells", 8);
        for (int i = 0; i < spellsList.size(); i++) loadedSpells.add(spellsList.getString(i));

        var sanitized = ProgressionStateRules.sanitize(
            new ProgressionStateRules.StoredState(
                tag.getString("ActiveSchool"),
                loadedLevels,
                loadedXp,
                loadedUnlocked,
                loadedSpells
            ),
            id -> SchoolRegistry.get(id) != null,
            spellId -> {
                var spell = SpellRegistry.get(spellId);
                return spell == null ? null : spell.school;
            }
        );

        activeSchool = sanitized.activeSchool();
        schoolLevels.clear();
        schoolLevels.putAll(sanitized.schoolLevels());
        schoolXp.clear();
        schoolXp.putAll(sanitized.schoolXp());
        unlockedSchools.clear();
        unlockedSchools.addAll(sanitized.unlockedSchools());
        learnedSpells.clear();
        learnedSpells.addAll(sanitized.learnedSpells());
    }
}
