package tong.sihriya.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpellEffectRulesTest {
    @Test
    void rangeBonusAmplifierStoresTenths() {
        assertEquals(4, SpellEffectRules.rangeBonusAmplifier(0.5f));
        assertEquals(0, SpellEffectRules.rangeBonusAmplifier(0.05f));
        assertEquals(9, SpellEffectRules.rangeBonusAmplifier(1.0f));
    }

    @Test
    void rangeMultiplierReadsTenthsFromAmplifier() {
        assertEquals(1.5, SpellEffectRules.rangeMultiplierFromAmplifier(4), 0.0001);
        assertEquals(1.1, SpellEffectRules.rangeMultiplierFromAmplifier(0), 0.0001);
        assertEquals(1.0, SpellEffectRules.rangeMultiplierFromAmplifier(-1), 0.0001);
    }

    @Test
    void orbitDamageAmplifierStoresHalfHeartsWithoutGoingNegative() {
        assertEquals(3, SpellEffectRules.orbitDamageAmplifier(2.0f));
        assertEquals(0, SpellEffectRules.orbitDamageAmplifier(0.2f));
        assertEquals(23, SpellEffectRules.orbitDamageAmplifier(12.0f));
    }

    @Test
    void orbitDamageReadsHalfHeartsFromAmplifier() {
        assertEquals(2.0f, SpellEffectRules.orbitDamageFromAmplifier(3), 0.0001f);
        assertEquals(0.5f, SpellEffectRules.orbitDamageFromAmplifier(0), 0.0001f);
        assertEquals(0.0f, SpellEffectRules.orbitDamageFromAmplifier(-1), 0.0001f);
    }

    @Test
    void magicFlightOnlyRevokesSurvivalFlight() {
        assertEquals(true, SpellEffectRules.shouldRevokeMagicFlight(false, false));
        assertEquals(false, SpellEffectRules.shouldRevokeMagicFlight(true, false));
        assertEquals(false, SpellEffectRules.shouldRevokeMagicFlight(false, true));
    }
}
