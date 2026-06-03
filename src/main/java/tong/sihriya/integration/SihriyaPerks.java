package tong.sihriya.integration;

import tong.sihriya.Sihriya;

/**
 * Perks magiques Sihriya — passifs automatiques basés sur les 9 stats magiques STAT Mod.
 * 27 perks répartis sur 9 stats (3 niveaux : 20, 50, 80 par stat).
 *
 * Implémentés dans SpellCastHandler via getPerkModifiers() et applyPerkAoEEffects().
 * Toutes les stats magiques STAT Mod sont prises en compte.
 */
public class SihriyaPerks {

    // ═══════════════════════════════════════════════════════
    // FIRE_AFFINITY (index 10)
    // ═══════════════════════════════════════════════════════
    // COMBUSTION (20) : +25% dégâts de feu (damage + burn)
    // INFERNO (50)    : Zone de feu AoE 3 blocs autour du joueur
    // PYROMANIA (80)  : 30% chance de propager le feu aux ennemis proches

    // ═══════════════════════════════════════════════════════
    // WATER_AFFINITY (index 8)
    // ═══════════════════════════════════════════════════════
    // GEYSER (20)     : +25% durée des slows/freeze
    // TOURBILLON (50) : Pousse les ennemis proches (4 blocs)
    // TSUNAMI (80)    : (non implémenté)

    // ═══════════════════════════════════════════════════════
    // AIR_AFFINITY (index 11)
    // ═══════════════════════════════════════════════════════
    // RAFALE (20)     : +50% knockback/pull
    // TEMPETE (50)    : Aspire les ennemis proches (6 blocs)
    // OURAGAN (80)    : (non implémenté)

    // ═══════════════════════════════════════════════════════
    // EARTH_AFFINITY (index 9)
    // ═══════════════════════════════════════════════════════
    // SISMIQUE (20)   : +25% durée de stun
    // ROCHER (50)     : Résistance temporaire (5s)
    // CATACLYSME (80) : Stun AoE 4 blocs

    // ═══════════════════════════════════════════════════════
    // ARCANE_POWER (index 7)
    // ═══════════════════════════════════════════════════════
    // FOUDRE (20)         : +25% dégâts magiques (tous les sorts)
    // TEMPETE_ARCANE (50) : +2 cibles chaîne
    // CATACLYSME_ARCANE (80) : Foudre AoE 5 blocs (50% dmg)

    // ═══════════════════════════════════════════════════════
    // MAGIC_RESISTANCE (index 12)
    // ═══════════════════════════════════════════════════════
    // SHIELD_ADEQUAT (20) : +15% durée absorb/damage_reduction
    // MUR_MAGIQUE (50)    : Dispel inflige aussi slowness
    // BOUCLIER_ARCANE (80): Absorption auto à chaque cast

    // ═══════════════════════════════════════════════════════
    // CASTING_SPEED (index 13)
    // ═══════════════════════════════════════════════════════
    // RAPIDITE (20)   : +30% distance dash
    // CANALISATION (50): +30% heal
    // PRESTO (80)     : Speed boost à chaque cast

    // ═══════════════════════════════════════════════════════
    // MANA_POOL (index 14)
    // ═══════════════════════════════════════════════════════
    // RESERVE (20)    : (passif via StatCalculator — déjà géré par getMaxMana)
    // RESERVOIR (50)  : +3 mana regen à chaque cast
    // FONTAINE (80)   : (non implémenté)

    // ═══════════════════════════════════════════════════════
    // ERUDITION (index 15)
    // ═══════════════════════════════════════════════════════
    // SAVANT (20)     : +15% durée buffs (speed, flight, melee, thorns)
    // MAITRE (50)     : +15% dégâts tous sorts
    // SAGE (80)       : +5 mana refund à chaque cast

    public static void init() {
        Sihriya.LOGGER.info("27 Sihriya magic perks active (9 STAT Mod magic stats)");
    }
}
