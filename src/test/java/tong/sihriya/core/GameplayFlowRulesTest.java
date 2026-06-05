package tong.sihriya.core;

import org.junit.jupiter.api.Test;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.sihriya.data.SpellRegistry.SpellEffect;
import tong.sihriya.data.SpellRegistry.SpellType;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameplayFlowRulesTest {

    @Test
    void progressionUnlocksSpellsAndSelectionPrefersBestCastableSpell() {
        var spells = List.of(
            spell("fire.spark", "fire", 1, 4, 10),
            spell("fire.nova", "fire", 2, 7, 8),
            spell("fire.sun", "fire", 3, 7, 4),
            spell("water.wave", "water", 1, 2, 1)
        );

        var progression = new SchoolProgression();
        progression.addXp("fire", 60_000);
        progression.unlockSchool("fire");
        progression.learnSpell("fire.spark");

        var unlocks = ProgressionUnlockRules.newlyUnlockedSpellIds(
            spells,
            progression::getLevel,
            progression.getLearnedSpells()
        );

        assertEquals(Set.of("fire.nova", "water.wave"), unlocks);

        unlocks.forEach(progression::learnSpell);

        var ranked = SpellSelectionRules.rankedCastCandidates(
            spells,
            progression.getLearnedSpells(),
            "fire"
        );

        assertEquals(2, ranked.size());
        assertEquals("fire.nova", ranked.get(0).id);
        assertEquals("fire.spark", ranked.get(1).id);
        assertTrue(ranked.stream().allMatch(spell -> "fire".equals(spell.school)));
    }

    private static SpellData spell(String id, String school, int tier, int manaCost, int cooldown) {
        return new SpellData(
            id,
            school,
            tier,
            manaCost,
            cooldown,
            0,
            SpellType.BUFF,
            List.of(new SpellEffect("speed", 1.0f, 0.0f, 20))
        );
    }
}
