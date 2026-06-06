package tong.sihriya.core;

import org.junit.jupiter.api.Test;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.sihriya.data.SpellRegistry.SpellEffect;
import tong.sihriya.data.SpellRegistry.SpellType;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellSelectionRulesTest {
    @Test
    void ranksOnlyLearnedSpellsFromRequestedSchool() {
        var fireLow = spell("fire.spark", "fire", 1, 5, 20);
        var fireHigh = spell("fire.nova", "fire", 3, 25, 120);
        var waterHigh = spell("water.deluge", "water", 5, 60, 500);

        var ranked = SpellSelectionRules.rankedCastCandidates(
            List.of(fireLow, fireHigh, waterHigh),
            Set.of("fire.spark", "fire.nova", "water.deluge"),
            "fire"
        );

        assertEquals(List.of(fireHigh, fireLow), ranked);
    }

    @Test
    void ordersSameTierCandidatesDeterministically() {
        var expensive = spell("fire.expensive", "fire", 2, 30, 80);
        var slow = spell("fire.slow", "fire", 2, 12, 200);
        var fast = spell("fire.fast", "fire", 2, 12, 40);
        var alpha = spell("fire.alpha", "fire", 2, 12, 40);

        var ranked = SpellSelectionRules.rankedCastCandidates(
            List.of(expensive, slow, fast, alpha),
            Set.of("fire.expensive", "fire.slow", "fire.fast", "fire.alpha"),
            "fire"
        );

        assertEquals(List.of(alpha, fast, slow, expensive), ranked);
    }

    @Test
    void returnsEmptyWhenNoLearnedSpellMatchesSchool() {
        var ranked = SpellSelectionRules.rankedCastCandidates(
            List.of(spell("fire.spark", "fire", 1, 5, 20)),
            Set.of("water.wave"),
            "fire"
        );

        assertTrue(ranked.isEmpty());
    }

    private static SpellData spell(String id, String school, int tier, int manaCost, int cooldown) {
        return new SpellData(
            id,
            school,
            tier,
            manaCost,
            cooldown,
            0,
            SpellType.PROJECTILE,
            List.of(new SpellEffect("damage", 1.0f, 0.0f, 0))
        );
    }
}
