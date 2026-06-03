package tong.sihriya.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import tong.sihriya.integration.STATModIntegration;

public class ManaManager implements INBTSerializable<CompoundTag> {
    private static final int BASE_MAX_MANA = 50;

    private float mana;
    private long lockUntil = 0; // system time when lock expires

    /** Max mana dynamique basé sur MANA_POOL de STAT Mod */
    public float getMaxMana(Player player) {
        return STATModIntegration.getMaxMana(player);
    }

    public float getMana() { return mana; }
    public float getManaPercent(Player player) {
        float max = getMaxMana(player);
        return max > 0 ? mana / max : 0;
    }

    public void setMana(float amount) { this.mana = Math.max(0, amount); }
    public void clampMana(Player player) { this.mana = Math.min(mana, getMaxMana(player)); }

    public boolean consumeMana(float amount) {
        if (mana < amount) return false;
        mana -= amount;
        return true;
    }

    public void regenMana(float amount) {
        if (isLocked()) return;
        mana += amount; // Le clamp au maxMana est fait par regenMana(float, Player) ou clampMana(Player)
    }

    /** Regen mana en fonction du joueur (utilise getMaxMana comme cap) */
    public void regenMana(float amount, Player player) {
        if (isLocked()) return;
        mana = Math.min(mana + amount, getMaxMana(player));
    }

    public void lockMana(long durationMs) {
        this.lockUntil = System.currentTimeMillis() + durationMs;
    }

    /** Lock mana avec réduction basée sur WILLPOWER */
    public void lockMana(long durationMs, Player player) {
        float reduction = STATModIntegration.getLockReduction(player);
        long reduced = (long)(durationMs * (1 - reduction));
        this.lockUntil = System.currentTimeMillis() + reduced;
    }

    public boolean isLocked() {
        return System.currentTimeMillis() < lockUntil;
    }

    public long getLockRemainingMs() {
        return Math.max(0, lockUntil - System.currentTimeMillis());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Mana", mana);
        tag.putLong("LockUntil", lockUntil);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.mana = tag.getFloat("Mana");
        this.lockUntil = tag.getLong("LockUntil");
    }
}
