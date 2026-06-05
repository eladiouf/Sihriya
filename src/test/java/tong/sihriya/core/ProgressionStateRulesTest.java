package tong.sihriya.core;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressionStateRulesTest {
    private static final Set<String> KNOWN_SCHOOLS = Set.of("fire", "water", "lava");
    private static final Map<String, String> KNOWN_SPELLS = Map.of(
        "fire.spark", "fire",
        "fire.nova", "fire",
        "water.wave", "water",
        "lava.flow", "lava"
    );

    @Test
    void removesUnknownSchoolsAndSpellsFromPersistedState() {
        var sanitized = ProgressionStateRules.sanitize(new ProgressionStateRules.StoredState(
            "shadow",
            Map.of("fire", 25, "shadow", 100),
            Map.of("fire", 20, "shadow", 999),
            Set.of("fire", "shadow"),
            Set.of("fire.spark", "shadow.hex", "water.wave")
        ), KNOWN_SCHOOLS::contains, KNOWN_SPELLS::get);

        assertEquals("", sanitized.activeSchool());
        assertEquals(Map.of("fire", 25), sanitized.schoolLevels());
        assertEquals(Map.of("fire", 20), sanitized.schoolXp());
        assertEquals(Set.of("fire"), sanitized.unlockedSchools());
        assertEquals(Set.of("fire.spark"), sanitized.learnedSpells());
    }

    @Test
    void keepsActiveSchoolOnlyWhenUnlocked() {
        var sanitized = ProgressionStateRules.sanitize(new ProgressionStateRules.StoredState(
            "water",
            Map.of("water", 10),
            Map.of("water", 5),
            Set.of("fire"),
            Set.of()
        ), KNOWN_SCHOOLS::contains, KNOWN_SPELLS::get);

        assertEquals("", sanitized.activeSchool());
    }

    @Test
    void clampsLevelsAndXpToValidProgressionBounds() {
        var sanitized = ProgressionStateRules.sanitize(new ProgressionStateRules.StoredState(
            "fire",
            Map.of("fire", 150, "water", -20, "lava", 1),
            Map.of("fire", 5000, "water", -5, "lava", 9999),
            Set.of("fire", "water", "lava"),
            Set.of("fire.spark", "water.wave", "lava.flow")
        ), KNOWN_SCHOOLS::contains, KNOWN_SPELLS::get);

        assertEquals(100, sanitized.schoolLevels().get("fire"));
        assertEquals(0, sanitized.schoolXp().get("fire"));
        assertEquals(0, sanitized.schoolLevels().get("water"));
        assertEquals(0, sanitized.schoolXp().get("water"));
        assertEquals(1, sanitized.schoolLevels().get("lava"));
        assertEquals(39, sanitized.schoolXp().get("lava"));
    }
}
