package tong.sihriya.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import tong.sihriya.Sihriya;
import tong.sihriya.core.ProgressionUnlockRules;
import tong.sihriya.core.SchoolProgression;
import tong.sihriya.core.SpellSelectionRules;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.data.SpellRegistry;

import static java.lang.String.format;

@GameTestHolder(Sihriya.MODID)
public final class SihriyaRuntimeGameTests {
    private SihriyaRuntimeGameTests() {}

    @PrefixGameTestTemplate(false)
    @GameTest(templateNamespace = "minecraft", template = "empty", timeoutTicks = 40)
    public static void coreRegistriesAndFlowBootInGame(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(SchoolRegistry.size() == 9, "expected 9 schools, found %d", SchoolRegistry.size());
            require(SpellRegistry.size() == 252, "expected 252 spells, found %d", SpellRegistry.size());
            require(SchoolRegistry.get("fire") != null, "missing fire school");
            require(SpellRegistry.get("fire.spark") != null, "missing fire.spark");

            var progression = new SchoolProgression();
            progression.unlockSchool("fire");
            progression.learnSpell("fire.spark");
            progression.setActiveSchool("fire");
            progression.addXp("fire", 100_000);

            require(progression.getLevel("fire") >= ProgressionUnlockRules.unlockLevelForTier(2),
                "fire level did not reach tier 2 unlock threshold");
            require(progression.isSchoolUnlocked("fire"), "fire school should be unlocked");
            require(progression.isSpellLearned("fire.spark"), "fire.spark should be learned");
            require("fire".equals(progression.getActiveSchool()), "active school should remain fire");

            var unlocks = ProgressionUnlockRules.newlyUnlockedSpellIds(
                SpellRegistry.getAll(),
                progression::getLevel,
                progression.getLearnedSpells());
            require(unlocks.contains("fire.fire_nova"), "expected fire.fire_nova to be unlockable");
            unlocks.forEach(progression::learnSpell);

            var ranked = SpellSelectionRules.rankedCastCandidates(
                SpellRegistry.getBySchool("fire"),
                progression.getLearnedSpells(),
                "fire");
            require(!ranked.isEmpty(), "expected ranked fire spell candidates");
            require("fire.flame_dance".equals(ranked.get(0).id),
                "expected fire.flame_dance to rank first, found %s", ranked.get(0).id);
        });
    }

    private static void require(boolean condition, String message, Object... args) {
        if (!condition) {
            throw new AssertionError(format(message, args));
        }
    }
}
