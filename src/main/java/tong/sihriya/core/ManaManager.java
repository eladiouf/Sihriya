package tong.sihriya.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import tong.statmod.capability.PlayerStatsProvider;
import tong.statmod.stats.StatType;

/**
 * Wrapper autour de STAT Mod PlayerStats pour le mana.
 * Délègue toutes les opérations à PlayerStats.getMana() / consumeMana() / regenMana().
 * Le max mana est piloté par MANA_POOL via StatCalculator.getManaBonus().
 */
public class ManaManager implements INBTSerializable<CompoundTag> {

    private static tong.statmod.capability.PlayerStats getStats(Player player) {
        return player.getCapability(PlayerStatsProvider.PLAYER_STATS).resolve().orElse(null);
    }

    public float getMaxMana(Player player) {
        var stats = getStats(player);
        return stats != null ? stats.getMaxMana() : 50;
    }

    public float getMana(Player player) {
        var stats = getStats(player);
        return stats != null ? stats.getMana() : 50;
    }

    public float getManaPercent(Player player) {
        float max = getMaxMana(player);
        return max > 0 ? getMana(player) / max : 0;
    }

    public void setMana(Player player, float amount) {
        var stats = getStats(player);
        if (stats != null) stats.setMana(amount);
    }

    public boolean consumeMana(Player player, float amount) {
        var stats = getStats(player);
        if (stats == null) return false;
        return stats.consumeMana(amount);
    }

    public void regenMana(Player player, float amount) {
        var stats = getStats(player);
        if (stats != null) stats.regenMana(amount);
    }

    public boolean isLocked(Player player) {
        var stats = getStats(player);
        return stats != null && stats.isManaBlocked();
    }

    public void lockMana(Player player, long durationMs) {
        var stats = getStats(player);
        if (stats != null) stats.blockMana(durationMs);
    }

    public long getLockRemainingMs(Player player) {
        var stats = getStats(player);
        return stats != null
            ? ManaInteropRules.remainingManaLockMs(stats.getManaBlockRemainingTicks())
            : 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag(); // NBT managed by STAT Mod PlayerStats
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        // NBT managed by STAT Mod PlayerStats
    }
}
