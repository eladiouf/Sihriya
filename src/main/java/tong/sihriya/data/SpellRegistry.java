package tong.sihriya.data;

import tong.sihriya.Sihriya;

import java.util.*;

public class SpellRegistry {
    private static final Map<String, SpellData> SPELLS = new LinkedHashMap<>();

    public static void register(SpellData spell) {
        SPELLS.put(spell.id, spell);
        Sihriya.LOGGER.debug("Registered spell: {}", spell.id);
    }

    public static SpellData get(String id) { return SPELLS.get(id); }
    public static Collection<SpellData> getAll() { return SPELLS.values(); }

    public static List<SpellData> getBySchool(String schoolId) {
        return SPELLS.values().stream().filter(s -> s.school.equals(schoolId)).toList();
    }

    public static List<SpellData> getBySchoolAndTier(String schoolId, int tier) {
        return SPELLS.values().stream().filter(s -> s.school.equals(schoolId) && s.tier == tier).toList();
    }

    public static int size() { return SPELLS.size(); }

    public static class SpellData {
        public final String id;
        public final String school;
        public final int tier;
        public final int manaCost;
        public final int cooldown; // ticks
        public final int castTime; // ticks (0 = instant)
        public final SpellType type;
        public final List<SpellEffect> effects;

        public SpellData(String id, String school, int tier, int manaCost, int cooldown,
                         int castTime, SpellType type, List<SpellEffect> effects) {
            this.id = id; this.school = school; this.tier = tier;
            this.manaCost = manaCost; this.cooldown = cooldown;
            this.castTime = castTime;
            this.type = type; this.effects = effects;
        }
    }

    public enum SpellType {
        PROJECTILE, ZONE, BUFF, SUMMON, ULTIMATE
    }

    public static class SpellEffect {
        public final String type; // "damage", "burn", "slow", "knockback", "stun", "freeze", "chain", "heal"
        public final float baseValue;
        public final float scaling; // per level of school
        public final int duration; // ticks

        public SpellEffect(String type, float baseValue, float scaling, int duration) {
            this.type = type; this.baseValue = baseValue;
            this.scaling = scaling; this.duration = duration;
        }
    }
}
