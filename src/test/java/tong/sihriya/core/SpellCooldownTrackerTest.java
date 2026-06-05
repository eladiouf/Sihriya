package tong.sihriya.core;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpellCooldownTrackerTest {
    @Test
    void reportsRemainingTicksForRecentCast() {
        SpellCooldownTracker tracker = new SpellCooldownTracker();
        UUID playerId = UUID.randomUUID();

        tracker.recordCast(playerId, "fire.spark", 1_000L);

        assertEquals(15, tracker.remainingCooldownTicks(playerId, "fire.spark", 20, 1_250L));
    }

    @Test
    void returnsZeroWhenCooldownExpiredOrUnknown() {
        SpellCooldownTracker tracker = new SpellCooldownTracker();
        UUID playerId = UUID.randomUUID();

        tracker.recordCast(playerId, "fire.spark", 1_000L);

        assertEquals(0, tracker.remainingCooldownTicks(playerId, "fire.spark", 20, 2_100L));
        assertEquals(0, tracker.remainingCooldownTicks(playerId, "fire.nova", 40, 2_100L));
    }

    @Test
    void removesTrackedCooldownsOnPlayerCleanup() {
        SpellCooldownTracker tracker = new SpellCooldownTracker();
        UUID playerId = UUID.randomUUID();

        tracker.recordCast(playerId, "fire.spark", 1_000L);
        tracker.clearPlayer(playerId);

        assertEquals(0, tracker.remainingCooldownTicks(playerId, "fire.spark", 20, 1_250L));
        assertEquals(0, tracker.trackedPlayerCount());
    }
}
