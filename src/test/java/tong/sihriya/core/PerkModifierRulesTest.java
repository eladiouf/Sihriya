package tong.sihriya.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerkModifierRulesTest {
    @Test
    void stacksFireArcaneAndEruditionDamageMultipliers() {
        var levels = PerkModifierRules.StatLevels.builder()
            .fire(20)
            .arcane(20)
            .erudition(50)
            .build();

        var modifier = PerkModifierRules.modifierFor(levels, "fire", "damage");

        assertEquals(1.25f * 1.25f * 1.15f, modifier.damageMult(), 0.0001f);
        assertEquals(1.0f, modifier.durationMult(), 0.0001f);
        assertEquals(0, modifier.extraTargets());
    }

    @Test
    void appliesAffinityDurationBonusesToControlEffects() {
        var water = PerkModifierRules.modifierFor(
            PerkModifierRules.StatLevels.builder().water(20).build(),
            "water",
            "slow"
        );
        var earth = PerkModifierRules.modifierFor(
            PerkModifierRules.StatLevels.builder().earth(20).build(),
            "earth",
            "stun"
        );

        assertEquals(1.25f, water.durationMult(), 0.0001f);
        assertEquals(1.25f, earth.durationMult(), 0.0001f);
    }

    @Test
    void appliesUtilityPerkBonuses() {
        var levels = PerkModifierRules.StatLevels.builder()
            .magicResistance(20)
            .castingSpeed(50)
            .erudition(20)
            .build();

        assertEquals(1.15f, PerkModifierRules.modifierFor(levels, "earth", "damage_reduction").durationMult(), 0.0001f);
        assertEquals(1.3f, PerkModifierRules.modifierFor(levels, "wind", "dash").damageMult(), 0.0001f);
        assertEquals(1.3f, PerkModifierRules.modifierFor(levels, "water", "heal").damageMult(), 0.0001f);
        assertEquals(1.15f, PerkModifierRules.modifierFor(levels, "wind", "speed").durationMult(), 0.0001f);
    }

    @Test
    void appliesExtraTargetPerksForChainAndDispel() {
        var levels = PerkModifierRules.StatLevels.builder()
            .arcane(50)
            .magicResistance(50)
            .build();

        assertEquals(2, PerkModifierRules.modifierFor(levels, "lightning", "chain").extraTargets());
        assertEquals(1, PerkModifierRules.modifierFor(levels, "lumamancy", "dispel").extraTargets());
    }

    @Test
    void noQualifiedPerkReturnsNeutralModifier() {
        var modifier = PerkModifierRules.modifierFor(
            PerkModifierRules.StatLevels.builder().build(),
            "fire",
            "damage"
        );

        assertEquals(1.0f, modifier.damageMult(), 0.0001f);
        assertEquals(1.0f, modifier.durationMult(), 0.0001f);
        assertEquals(0, modifier.extraTargets());
    }
}
