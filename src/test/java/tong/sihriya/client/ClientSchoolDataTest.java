package tong.sihriya.client;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientSchoolDataTest {

    @Test
    void applySync_copiesMutableInputsDefensively() throws ReflectiveOperationException {
        setField("activeSchool", "");
        setField("schoolLevels", new HashMap<String, Integer>());
        setField("unlockedSchools", new HashSet<String>());
        setField("learnedSpells", new HashSet<String>());

        Map<String, Integer> levels = new HashMap<>();
        levels.put("fire", 0);
        Set<String> unlocked = new HashSet<>();
        Set<String> spells = new HashSet<>();

        ClientSchoolData.applySync("fire", levels, unlocked, spells);

        setField("activeSchool", "fire");
        setField("schoolLevels", new HashMap<>(levels));
        setField("unlockedSchools", new HashSet<>(Set.of("fire")));
        setField("learnedSpells", new HashSet<>(Set.of("fire.spark")));

        levels.put("fire", 12);
        unlocked.add("fire");
        spells.add("fire.spark");

        ClientSchoolData.applySync("fire", levels, unlocked, spells);

        levels.put("fire", 99);
        unlocked.add("water");
        spells.add("water.wave");

        assertEquals("fire", ClientSchoolData.getActiveSchool());
        assertEquals(12, ClientSchoolData.getLevel("fire"));
        assertTrue(ClientSchoolData.isUnlocked("fire"));
        assertFalse(ClientSchoolData.isUnlocked("water"));
        assertTrue(ClientSchoolData.isSpellLearned("fire.spark"));
        assertFalse(ClientSchoolData.isSpellLearned("water.wave"));
    }

    private static void setField(String name, Object value) throws ReflectiveOperationException {
        Field field = ClientSchoolData.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }
}
