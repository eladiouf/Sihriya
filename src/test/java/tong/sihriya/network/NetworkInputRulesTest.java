package tong.sihriya.network;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkInputRulesTest {
    private static final Set<String> KNOWN_SCHOOLS = Set.of("fire", "water", "necromancy", "lumamancy");

    @Test
    void acceptsKnownSchoolIds() {
        assertTrue(NetworkInputRules.isValidSchoolId("fire", KNOWN_SCHOOLS::contains));
        assertTrue(NetworkInputRules.isValidSchoolId("necromancy", KNOWN_SCHOOLS::contains));
    }

    @Test
    void rejectsBlankMalformedAndLongSchoolIds() {
        assertFalse(NetworkInputRules.isValidSchoolId(null, KNOWN_SCHOOLS::contains));
        assertFalse(NetworkInputRules.isValidSchoolId("", KNOWN_SCHOOLS::contains));
        assertFalse(NetworkInputRules.isValidSchoolId("Fire", KNOWN_SCHOOLS::contains));
        assertFalse(NetworkInputRules.isValidSchoolId("fire.cast", KNOWN_SCHOOLS::contains));
        assertFalse(NetworkInputRules.isValidSchoolId("fire/../lava", KNOWN_SCHOOLS::contains));
        assertFalse(NetworkInputRules.isValidSchoolId("a".repeat(NetworkInputRules.MAX_SCHOOL_ID_LENGTH + 1),
            KNOWN_SCHOOLS::contains));
    }

    @Test
    void rejectsUnknownSchoolIdsEvenWhenSyntaxIsValid() {
        assertFalse(NetworkInputRules.isValidSchoolId("shadow", KNOWN_SCHOOLS::contains));
    }

    @Test
    void validatesSchoolAndSpellSyntaxWithoutRegistryLookup() {
        assertTrue(NetworkInputRules.isValidSchoolIdSyntax("fire"));
        assertFalse(NetworkInputRules.isValidSchoolIdSyntax("Fire"));
        assertTrue(NetworkInputRules.isValidSpellId("fire.spark"));
        assertTrue(NetworkInputRules.isValidSpellId("necromancy.soul_scythe"));
        assertFalse(NetworkInputRules.isValidSpellId("fire"));
        assertFalse(NetworkInputRules.isValidSpellId("fire/../spark"));
    }

    @Test
    void acceptsOnlyReasonableSyncEntryCounts() {
        assertTrue(NetworkInputRules.requireSyncEntryCount(0) == 0);
        assertTrue(NetworkInputRules.requireSyncEntryCount(NetworkInputRules.MAX_SYNC_ENTRIES)
            == NetworkInputRules.MAX_SYNC_ENTRIES);
        assertThrows(IllegalArgumentException.class, () -> NetworkInputRules.requireSyncEntryCount(-1));
        assertThrows(IllegalArgumentException.class,
            () -> NetworkInputRules.requireSyncEntryCount(NetworkInputRules.MAX_SYNC_ENTRIES + 1));
    }
}
