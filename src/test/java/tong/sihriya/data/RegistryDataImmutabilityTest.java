package tong.sihriya.data;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistryDataImmutabilityTest {
    @Test
    void spellDataCopiesEffectsAndExposesReadOnlyList() {
        List<SpellRegistry.SpellEffect> sourceEffects = new ArrayList<>();
        sourceEffects.add(new SpellRegistry.SpellEffect("damage", 1.0f, 0.0f, 0));

        SpellRegistry.SpellData spell = new SpellRegistry.SpellData(
            "fire.spark",
            "fire",
            1,
            5,
            20,
            0,
            SpellRegistry.SpellType.PROJECTILE,
            sourceEffects
        );

        sourceEffects.clear();

        assertEquals(1, spell.effects.size());
        assertThrows(UnsupportedOperationException.class,
            () -> spell.effects.add(new SpellRegistry.SpellEffect("burn", 1.0f, 0.0f, 20)));
    }

    @Test
    void unlockConditionCopiesArraysAndReturnsDefensiveClones() {
        String[] schoolIds = {"fire", "wind"};
        int[] levels = {50, 75};

        SchoolRegistry.UnlockCondition unlock = new SchoolRegistry.UnlockCondition("or", schoolIds, levels);

        schoolIds[0] = "shadow";
        levels[0] = 999;

        assertArrayEquals(new String[]{"fire", "wind"}, unlock.schoolIds());
        assertArrayEquals(new int[]{50, 75}, unlock.levels());

        String[] exportedSchoolIds = unlock.schoolIds();
        int[] exportedLevels = unlock.levels();
        exportedSchoolIds[1] = "water";
        exportedLevels[1] = 1;

        assertArrayEquals(new String[]{"fire", "wind"}, unlock.schoolIds());
        assertArrayEquals(new int[]{50, 75}, unlock.levels());
    }
}
