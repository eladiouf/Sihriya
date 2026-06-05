package tong.sihriya.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerkCastRulesTest {
    @Test
    void manaPoolRegenUsesSingleBestTier() {
        assertEquals(0, PerkCastRules.manaRefundFor(0));
        assertEquals(3, PerkCastRules.manaRefundFor(50));
        assertEquals(8, PerkCastRules.manaRefundFor(80));
    }

    @Test
    void waterTierEightyAddsTsunamiControl() {
        var actions = PerkCastRules.actionsFor(
            PerkCastRules.StatLevels.builder().water(80).build(),
            "water"
        );

        assertTrue(actions.stream().anyMatch(action -> action.type() == PerkCastRules.ActionType.WATER_PUSH));
        assertTrue(actions.stream().anyMatch(action -> action.type() == PerkCastRules.ActionType.TSUNAMI_SLOW));
    }

    @Test
    void airTierEightyAddsHurricaneLift() {
        var actions = PerkCastRules.actionsFor(
            PerkCastRules.StatLevels.builder().air(80).build(),
            "wind"
        );

        assertTrue(actions.stream().anyMatch(action -> action.type() == PerkCastRules.ActionType.WIND_PULL));
        assertTrue(actions.stream().anyMatch(action -> action.type() == PerkCastRules.ActionType.HURRICANE_LIFT));
    }

    @Test
    void unrelatedSchoolDoesNotTriggerAffinityAoE() {
        var actions = PerkCastRules.actionsFor(
            PerkCastRules.StatLevels.builder().water(80).air(80).build(),
            "earth"
        );

        assertTrue(actions.stream().noneMatch(action -> action.type() == PerkCastRules.ActionType.TSUNAMI_SLOW));
        assertTrue(actions.stream().noneMatch(action -> action.type() == PerkCastRules.ActionType.HURRICANE_LIFT));
    }
}
