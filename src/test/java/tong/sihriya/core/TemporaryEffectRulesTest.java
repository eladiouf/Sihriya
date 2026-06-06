package tong.sihriya.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemporaryEffectRulesTest {
    @Test
    void summonCountDefaultsToOneAndCapsServerLoad() {
        assertEquals(1, TemporaryEffectRules.clampSummonCount(0));
        assertEquals(1, TemporaryEffectRules.clampSummonCount(-5));
        assertEquals(5, TemporaryEffectRules.clampSummonCount(5));
        assertEquals(TemporaryEffectRules.MAX_SUMMON_COUNT, TemporaryEffectRules.clampSummonCount(30));
    }

    @Test
    void summonDurationDefaultsAndCapsLongLivedEntities() {
        assertEquals(TemporaryEffectRules.DEFAULT_SUMMON_DURATION_TICKS,
            TemporaryEffectRules.clampSummonDuration(0));
        assertEquals(TemporaryEffectRules.DEFAULT_SUMMON_DURATION_TICKS,
            TemporaryEffectRules.clampSummonDuration(-1));
        assertEquals(1200, TemporaryEffectRules.clampSummonDuration(1200));
        assertEquals(TemporaryEffectRules.MAX_SUMMON_DURATION_TICKS,
            TemporaryEffectRules.clampSummonDuration(6000));
    }

    @Test
    void wallDurationDefaultsAndCapsTemporaryBlocks() {
        assertEquals(TemporaryEffectRules.DEFAULT_WALL_DURATION_TICKS,
            TemporaryEffectRules.clampWallDuration(0));
        assertEquals(TemporaryEffectRules.DEFAULT_WALL_DURATION_TICKS,
            TemporaryEffectRules.clampWallDuration(-40));
        assertEquals(700, TemporaryEffectRules.clampWallDuration(700));
        assertEquals(TemporaryEffectRules.MAX_TEMPORARY_BLOCK_TICKS,
            TemporaryEffectRules.clampWallDuration(6000));
    }

    @Test
    void summonPlacementUsesClampedCountForSpacing() {
        int requestedCount = 30;
        int actualCount = TemporaryEffectRules.clampSummonCount(requestedCount);

        assertEquals(Math.PI, TemporaryEffectRules.summonAngle(6, actualCount), 0.0001);
    }
}
