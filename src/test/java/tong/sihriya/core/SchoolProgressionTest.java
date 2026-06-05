package tong.sihriya.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SchoolProgressionTest {
    @Test
    void unlockedSchoolsViewCannotBeMutatedExternally() {
        SchoolProgression progression = new SchoolProgression();
        progression.unlockSchool("fire");

        var unlockedSchools = progression.getUnlockedSchools();

        assertThrows(UnsupportedOperationException.class, () -> unlockedSchools.add("water"));
        assertEquals(1, progression.getUnlockedSchools().size());
    }

    @Test
    void learnedSpellsViewCannotBeMutatedExternally() {
        SchoolProgression progression = new SchoolProgression();
        progression.learnSpell("fire.spark");

        var learnedSpells = progression.getLearnedSpells();

        assertThrows(UnsupportedOperationException.class, () -> learnedSpells.remove("fire.spark"));
        assertEquals(1, progression.getLearnedSpells().size());
    }
}
