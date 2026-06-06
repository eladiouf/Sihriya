package tong.sihriya.core;

import org.junit.jupiter.api.Test;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.sihriya.data.SpellRegistry.SpellEffect;
import tong.sihriya.data.SpellRegistry.SpellType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameplayPerformanceBudgetTest {
    private static final int SPELLS_PER_SCHOOL = 1200;
    private static final int ITERATIONS = 20;
    private static final long BUDGET_NANOS = TimeUnit.MILLISECONDS.toNanos(1200);

    @Test
    void coreGameplayRuleEvaluationStaysWithinBudget() {
        var spells = syntheticSpells();

        var fireKnownSpells = new HashSet<String>();
        for (var spell : spells) {
            if ("fire".equals(spell.school)) {
                fireKnownSpells.add(spell.id);
            }
        }

        long start = System.nanoTime();
        Set<String> unlocks = Set.of();
        List<SpellData> ranked = List.of();
        for (int i = 0; i < ITERATIONS; i++) {
            unlocks = ProgressionUnlockRules.newlyUnlockedSpellIds(
                spells,
                school -> 100,
                Set.of()
            );
            ranked = SpellSelectionRules.rankedCastCandidates(
                spells,
                fireKnownSpells,
                "fire"
            );
        }
        long elapsed = System.nanoTime() - start;

        assertEquals(spells.size(), unlocks.size());
        assertEquals(SPELLS_PER_SCHOOL, ranked.size());
        assertEquals("fire", ranked.get(0).school);
        assertTrue(elapsed <= BUDGET_NANOS,
            "Gameplay rule evaluation took " + TimeUnit.NANOSECONDS.toMillis(elapsed)
                + "ms, budget is " + TimeUnit.NANOSECONDS.toMillis(BUDGET_NANOS) + "ms");
    }

    private static List<SpellData> syntheticSpells() {
        var spells = new ArrayList<SpellData>(SPELLS_PER_SCHOOL * 9);
        String[] schools = {"fire", "water", "earth", "air", "lightning", "lumamancy", "necromancy", "ice", "lava"};
        for (String school : schools) {
            for (int i = 0; i < SPELLS_PER_SCHOOL; i++) {
                spells.add(new SpellData(
                    school + ".spell_" + i,
                    school,
                    (i % 5) + 1,
                    3 + (i % 7),
                    4 + (i % 11),
                    0,
                    SpellType.BUFF,
                    List.of(new SpellEffect("speed", 1.0f, 0.0f, 20))
                ));
            }
        }
        return spells;
    }
}
