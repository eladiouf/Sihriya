package tong.sihriya.core;

import tong.sihriya.data.SpellRegistry.SpellData;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class SpellSelectionRules {
    private SpellSelectionRules() {}

    public static List<SpellData> rankedCastCandidates(
            Collection<SpellData> spells,
            Set<String> learnedSpellIds,
            String schoolId) {
        return spells.stream()
            .filter(spell -> learnedSpellIds.contains(spell.id))
            .filter(spell -> schoolId.equals(spell.school))
            .sorted(Comparator
                .comparingInt((SpellData spell) -> spell.tier).reversed()
                .thenComparingInt(spell -> spell.manaCost)
                .thenComparingInt(spell -> spell.cooldown)
                .thenComparing(spell -> spell.id))
            .toList();
    }
}
