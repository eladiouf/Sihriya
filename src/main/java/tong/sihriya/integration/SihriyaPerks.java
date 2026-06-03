package tong.sihriya.integration;

import tong.sihriya.Sihriya;

/**
 * Perks magiques Sihriya — s'intègrent dans l'infrastructure STAT Mod.
 * 3 perks par stat élémentaire (niveaux 20, 50, 80).
 *
 * Stub pour l'instant — l'intégration complète nécessite de vérifier
 * l'API Perk disponible dans STAT Mod.
 */
public class SihriyaPerks {

    // FIRE_AFFINITY perks
    // COMBUSTION (niveau 20) : +25% dégâts de feu
    // INFERNO (niveau 50) : Zone de feu AoE autour du joueur
    // PYROMANIA (niveau 80) : Brûlure se propage aux ennemis proches

    // WATER_AFFINITY perks
    // GEYSER (niveau 20) : +25% durée des slows
    // TOURBILLON (niveau 50) : Pousse les ennemis avec l'eau
    // TSUNAMI (niveau 80) : Stun + dégâts de zone

    // AIR_AFFINITY perks
    // RAFALE (niveau 20) : +50% knockback
    // TEMPETE (niveau 50) : Tornade aspirante
    // OURAGAN (niveau 80) : Knockback + dégâts AoE

    // EARTH_AFFINITY perks
    // SISMIQUE (niveau 20) : +25% durée stun
    // ROCHER (niveau 50) : Bouclier de pierre
    // CATACLYSME (niveau 80) : AoE stun + dégâts

    // ARCANE_POWER perks
    // FOUDRE (niveau 20) : +25% dégâts magiques
    // TEMPETE_ARCANE (niveau 50) : Chaîne +2 cibles
    // CATACLYSME_ARCANE (niveau 80) : AoE foudre géant

    public static void registerAll() {
        // Les perks seront enregistrés via le système STAT Mod
        Sihriya.LOGGER.info("15 Sihriya magic perks ready for STAT Mod integration");
    }
}
