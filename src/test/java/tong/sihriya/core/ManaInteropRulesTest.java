package tong.sihriya.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManaInteropRulesTest {
    @Test
    void convertsRemainingTicksToMilliseconds() {
        assertEquals(0L, ManaInteropRules.remainingManaLockMs(0));
        assertEquals(50L, ManaInteropRules.remainingManaLockMs(1));
        assertEquals(1_500L, ManaInteropRules.remainingManaLockMs(30));
    }

    @Test
    void clampsNegativeRemainingTicksToZero() {
        assertEquals(0L, ManaInteropRules.remainingManaLockMs(-1));
        assertEquals(0L, ManaInteropRules.remainingManaLockMs(-20));
    }
}
