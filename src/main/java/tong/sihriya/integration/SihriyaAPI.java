package tong.sihriya.integration;

import tong.sihriya.Sihriya;

/**
 * Bridge optionnel vers STAT Mod.
 * S'il est présent, les stats magiques de STAT Mod influencent le scaling des sorts.
 */
public class SihriyaAPI {
    private static boolean statModPresent = false;

    public static void detectStatMod() {
        try {
            Class.forName("tong.statmod.stats.StatType");
            statModPresent = true;
            Sihriya.LOGGER.info("STAT Mod détecté ! Les sorts Sihriya bénéficieront des stats.");
        } catch (ClassNotFoundException e) {
            statModPresent = false;
            Sihriya.LOGGER.info("STAT Mod non trouvé. Scaling solo uniquement.");
        }
    }

    public static boolean isStatModPresent() { return statModPresent; }

    /**
     * Récupère le niveau d'une stat STAT Mod pour un joueur.
     * Retourne -1 si STAT Mod absent.
     */
    public static int getStatLevel(Object player, String statName) {
        if (!statModPresent) return -1;
        try {
            var caps = player.getClass().getMethod("getCapability", Class.class);
            var provider = Class.forName("tong.statmod.capability.PlayerStatsProvider");
            var field = provider.getField("PLAYER_STATS");
            var capInstance = caps.invoke(player, field.get(null));
            var resolve = capInstance.getClass().getMethod("resolve");
            var opt = resolve.invoke(capInstance);
            var present = opt.getClass().getMethod("isPresent");
            if ((boolean) present.invoke(opt)) {
                var get = opt.getClass().getMethod("get");
                var stats = get.invoke(opt);
                var statTypeEnum = Class.forName("tong.statmod.stats.StatType");
                var valueOf = statTypeEnum.getMethod("valueOf", String.class);
                var stat = valueOf.invoke(null, statName);
                var indexField = stat.getClass().getField("index");
                int idx = indexField.getInt(stat);
                var getLevel = stats.getClass().getMethod("getLevel", int.class);
                return (int) getLevel.invoke(stats, idx);
            }
        } catch (Exception e) {
            Sihriya.LOGGER.debug("Failed to query STAT Mod stat: {}", e.getMessage());
        }
        return -1;
    }

    /**
     * Calcule le bonus de scaling pour une école donnée.
     * Mapping: fire→FIRE_AFFINITY, water→WATER_AFFINITY, wind→AIR_AFFINITY,
     * earth→EARTH_AFFINITY, lightning→ARCANE_POWER, ice→WATER_AFFINITY
     */
    public static float getScalingBonus(Object player, String schoolId) {
        if (!statModPresent) return 0;
        String statName = switch (schoolId) {
            case "fire" -> "FIRE_AFFINITY";
            case "water" -> "WATER_AFFINITY";
            case "wind" -> "AIR_AFFINITY";
            case "earth" -> "EARTH_AFFINITY";
            case "lightning" -> "ARCANE_POWER";
            case "ice" -> "WATER_AFFINITY";
            default -> null;
        };
        if (statName == null) return 0;
        int level = getStatLevel(player, statName);
        if (level <= 0) return 0;
        return level * 0.002f; // +0.2% per level
    }
}
