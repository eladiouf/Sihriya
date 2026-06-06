package tong.sihriya.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.data.SpellRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchoolProgressionMigrationTest {
    private static final String FIRE_SCHOOL = "migration_fire";
    private static final String WATER_SCHOOL = "migration_water";
    private static final String FIRE_SPELL = "migration_fire.spark";
    private static final String WATER_SPELL = "migration_water.wave";

    @BeforeAll
    static void registerFixtureData() {
        SchoolRegistry.register(new SchoolRegistry.SchoolData(
            FIRE_SCHOOL,
            "Migration Fire",
            true,
            "#ff5500",
            new SchoolRegistry.UnlockCondition("level", new String[0], new int[0])
        ));
        SchoolRegistry.register(new SchoolRegistry.SchoolData(
            WATER_SCHOOL,
            "Migration Water",
            false,
            "#3366ff",
            new SchoolRegistry.UnlockCondition("level", new String[] { FIRE_SCHOOL }, new int[] { 10 })
        ));
        SpellRegistry.register(new SpellRegistry.SpellData(
            FIRE_SPELL,
            FIRE_SCHOOL,
            1,
            10,
            20,
            0,
            SpellRegistry.SpellType.PROJECTILE,
            java.util.List.of()
        ));
        SpellRegistry.register(new SpellRegistry.SpellData(
            WATER_SPELL,
            WATER_SCHOOL,
            1,
            10,
            20,
            0,
            SpellRegistry.SpellType.PROJECTILE,
            java.util.List.of()
        ));
    }

    @Test
    void legacyFixtureDeserializesToSanitizedRoundTripState() throws Exception {
        SchoolProgression progression = new SchoolProgression();

        progression.deserializeNBT(loadFixture("fixtures/migration/school-progression-legacy.snbt"));

        assertEquals("", progression.getActiveSchool());
        assertEquals(12, progression.getLevel(FIRE_SCHOOL));
        assertEquals(55, progression.getXp(FIRE_SCHOOL));
        assertEquals(0, progression.getLevel(WATER_SCHOOL));
        assertEquals(0, progression.getXp(WATER_SCHOOL));
        assertTrue(progression.isSchoolUnlocked(FIRE_SCHOOL));
        assertFalse(progression.isSchoolUnlocked("legacy_shadow"));
        assertTrue(progression.isSpellLearned(FIRE_SPELL));
        assertFalse(progression.isSpellLearned(WATER_SPELL));
        assertFalse(progression.isSpellLearned("legacy_shadow.hex"));

        CompoundTag migrated = progression.serializeNBT();
        assertEquals("", migrated.getString("ActiveSchool"));
        assertEquals(Map.of(FIRE_SCHOOL, 12, WATER_SCHOOL, 0), readLevelMap(migrated.getList("SchoolLevels", 10)));
        assertEquals(Map.of(FIRE_SCHOOL, 55, WATER_SCHOOL, 0), readXpMap(migrated.getList("SchoolLevels", 10)));
        assertEquals(Set.of(FIRE_SCHOOL), readStringSet(migrated.getList("UnlockedSchools", 8)));
        assertEquals(Set.of(FIRE_SPELL), readStringSet(migrated.getList("LearnedSpells", 8)));
    }

    private static CompoundTag loadFixture(String resourcePath) throws Exception {
        try (InputStream inputStream = SchoolProgressionMigrationTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Missing test fixture: " + resourcePath);
            }
            String snbt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return TagParser.parseTag(snbt);
        }
    }

    private static Map<String, Integer> readLevelMap(ListTag levels) {
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < levels.size(); i++) {
            CompoundTag entry = levels.getCompound(i);
            result.put(entry.getString("School"), entry.getInt("Level"));
        }
        return result;
    }

    private static Map<String, Integer> readXpMap(ListTag levels) {
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < levels.size(); i++) {
            CompoundTag entry = levels.getCompound(i);
            result.put(entry.getString("School"), entry.getInt("XP"));
        }
        return result;
    }

    private static Set<String> readStringSet(ListTag values) {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < values.size(); i++) {
            result.add(values.getString(i));
        }
        return result;
    }
}
