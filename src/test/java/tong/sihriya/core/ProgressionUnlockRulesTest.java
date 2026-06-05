package tong.sihriya.core;

import org.junit.jupiter.api.Test;
import tong.sihriya.data.SchoolRegistry.SchoolData;
import tong.sihriya.data.SchoolRegistry.UnlockCondition;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.sihriya.data.SpellRegistry.SpellEffect;
import tong.sihriya.data.SpellRegistry.SpellType;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressionUnlockRulesTest {
    @Test
    void exposesCanonicalTierUnlockLevels() {
        assertEquals(0, ProgressionUnlockRules.unlockLevelForTier(1));
        assertEquals(25, ProgressionUnlockRules.unlockLevelForTier(2));
        assertEquals(50, ProgressionUnlockRules.unlockLevelForTier(3));
        assertEquals(75, ProgressionUnlockRules.unlockLevelForTier(4));
        assertEquals(100, ProgressionUnlockRules.unlockLevelForTier(5));
        assertEquals(999, ProgressionUnlockRules.unlockLevelForTier(0));
        assertEquals(999, ProgressionUnlockRules.unlockLevelForTier(6));
    }

    @Test
    void findsNewSpellUnlocksAtReachedTiersOnly() {
        var spells = List.of(
            spell("fire.spark", "fire", 1),
            spell("fire.nova", "fire", 2),
            spell("fire.sun", "fire", 4),
            spell("water.wave", "water", 2)
        );

        var unlocks = ProgressionUnlockRules.newlyUnlockedSpellIds(
            spells,
            school -> switch (school) {
                case "fire" -> 25;
                case "water" -> 24;
                default -> 0;
            },
            Set.of("fire.spark")
        );

        assertEquals(Set.of("fire.nova"), unlocks);
    }

    @Test
    void findsAdvancedSchoolUnlocksForAndOrConditions() {
        var schools = List.of(
            school("fire", true, null),
            school("wind", true, null),
            school("earth", true, null),
            school("lightning", false, new UnlockCondition("or", new String[]{"fire", "wind"}, new int[]{50, 50})),
            school("lava", false, new UnlockCondition("and", new String[]{"fire", "earth"}, new int[]{50, 50}))
        );

        var unlocks = ProgressionUnlockRules.newlyUnlockedSchoolIds(
            schools,
            school -> switch (school) {
                case "fire" -> 50;
                case "earth" -> 49;
                default -> 0;
            },
            Set.of("fire", "wind", "earth")
        );

        assertEquals(Set.of("lightning"), unlocks);
    }

    @Test
    void doesNotReturnAlreadyUnlockedAdvancedSchools() {
        var schools = List.of(
            school("lightning", false, new UnlockCondition("or", new String[]{"fire", "wind"}, new int[]{50, 50}))
        );

        var unlocks = ProgressionUnlockRules.newlyUnlockedSchoolIds(
            schools,
            school -> 100,
            Set.of("lightning")
        );

        assertEquals(Set.of(), unlocks);
    }

    private static SchoolData school(String id, boolean starting, UnlockCondition unlock) {
        return new SchoolData(id, id, starting, "FFFFFF", unlock);
    }

    private static SpellData spell(String id, String school, int tier) {
        return new SpellData(
            id,
            school,
            tier,
            1,
            1,
            0,
            SpellType.BUFF,
            List.of(new SpellEffect("speed", 1.0f, 0.0f, 20))
        );
    }
}
