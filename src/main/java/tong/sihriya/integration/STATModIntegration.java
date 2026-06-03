package tong.sihriya.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.statmod.capability.PlayerStatsProvider;
import tong.statmod.progression.ActionXpHelper;
import tong.statmod.progression.ActionXpHelper.XpTier;
import tong.statmod.stats.StatCalculator;
import tong.statmod.stats.StatType;

public class STATModIntegration {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        try {
            Class.forName("tong.statmod.STATMod");
            Sihriya.LOGGER.info("STAT Mod détecté et intégré !");
            initialized = true;
        } catch (ClassNotFoundException e) {
            Sihriya.LOGGER.error("STAT Mod est requis mais introuvable !");
            throw new RuntimeException("STAT Mod manquant");
        }
    }

    public static boolean isInitialized() { return initialized; }

    /** Récupère le niveau d'une stat STAT Mod pour un joueur. */
    public static int getStatLevel(Player player, StatType stat) {
        if (!initialized) return 0;
        return player.getCapability(PlayerStatsProvider.PLAYER_STATS)
            .map(stats -> stats.getLevel(stat.index))
            .orElse(0);
    }

    /** Calcule le multiplicateur de dégâts pour une école donnée. */
    public static float getDamageMultiplier(Player player, String schoolId) {
        if (!initialized) return 1.0f;
        StatType stat = schoolToStat(schoolId);
        if (stat == null) return 1.0f;
        int level = getStatLevel(player, stat);
        return StatCalculator.getMagicDamageBonus(level);
    }

    /** Calcule le temps de cast (en ticks) pour un sort. */
    public static int getCastTime(Player player, SpellData spell) {
        if (!initialized) return spell.castTime;
        int castingSpeed = getStatLevel(player, StatType.CASTING_SPEED);
        float speedBonus = StatCalculator.getItemUseSpeed(castingSpeed);
        return Math.max(1, (int) (spell.castTime * (1 - speedBonus)));
    }

    /** Calcule le coût en mana pour un sort. */
    public static int getManaCost(Player player, SpellData spell) {
        if (!initialized) return spell.manaCost;
        int erudition = getStatLevel(player, StatType.ERUDITION);
        float reduction = StatCalculator.getXpBonus(erudition);
        return Math.max(1, (int) (spell.manaCost * (1 - reduction * 0.5f)));
    }

    /** Récupère le max mana d'un joueur (base 50 + bonus MANA_POOL). */
    public static int getMaxMana(Player player) {
        if (!initialized) return 50;
        int manaPoolLevel = getStatLevel(player, StatType.MANA_POOL);
        return 50 + StatCalculator.getManaBonus(manaPoolLevel);
    }

    /** Récupère le bonus XP pour une école (basé sur ERUDITION). */
    public static float getXpMultiplier(Player player) {
        if (!initialized) return 1.0f;
        int erudition = getStatLevel(player, StatType.ERUDITION);
        return 1.0f + StatCalculator.getXpBonus(erudition);
    }

    /** Récupère la réduction de lock-out mana (basé sur WILLPOWER). */
    public static float getLockReduction(Player player) {
        if (!initialized) return 0.0f;
        int willpower = getStatLevel(player, StatType.WILLPOWER);
        return StatCalculator.getStatusDurationReduction(willpower);
    }

    /** Récompense XP aux stats STAT Mod après un cast de sort. */
    public static void awardSpellXp(ServerPlayer player, SpellData spell) {
        if (!initialized) return;
        // XP à la stat d'affinité de l'école
        StatType stat = schoolToStat(spell.school);
        if (stat != null) {
            XpTier tier = switch (spell.tier) {
                case 1 -> XpTier.COMMON;
                case 2 -> XpTier.INTERMEDIATE;
                case 3 -> XpTier.RARE;
                default -> XpTier.COMMON;
            };
            ActionXpHelper.awardXp(player, stat.index, tier);
        }
        // XP à MANA_POOL
        ActionXpHelper.awardXp(player, StatType.MANA_POOL.index, XpTier.COMMON);
        // XP à CASTING_SPEED
        ActionXpHelper.awardXp(player, StatType.CASTING_SPEED.index, XpTier.COMMON);
    }

    /** Résistance magique de la cible (réduit les dégâts magiques reçus). */
    public static float getMagicResistance(Player target) {
        if (!initialized) return 0.0f;
        int level = getStatLevel(target, StatType.MAGIC_RESISTANCE);
        return StatCalculator.getMagicReduction(level);
    }

    /** Mappe un schoolId vers le StatType correspondant. */
    public static StatType schoolToStat(String schoolId) {
        return switch (schoolId) {
            case "fire" -> StatType.FIRE_AFFINITY;
            case "water" -> StatType.WATER_AFFINITY;
            case "wind" -> StatType.AIR_AFFINITY;
            case "earth" -> StatType.EARTH_AFFINITY;
            case "lightning" -> StatType.ARCANE_POWER;
            case "ice" -> StatType.WATER_AFFINITY;
            case "lava" -> StatType.FIRE_AFFINITY;
            case "necromancy" -> StatType.ARCANE_POWER;
            case "lumamancy" -> StatType.ERUDITION;
            default -> null;
        };
    }
}
