package tong.sihriya.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class ManaManager implements INBTSerializable<CompoundTag> {
    private static final int BASE_MAX_MANA = 50;

    private float mana;
    private float maxMana = BASE_MAX_MANA;
    private long lockUntil = 0; // system time when lock expires

    public float getMana() { return mana; }
    public float getMaxMana() { return maxMana; }
    public float getManaPercent() { return maxMana > 0 ? mana / maxMana : 0; }

    public void setMana(float amount) { this.mana = Math.min(amount, maxMana); }
    public void setMaxMana(float amount) { this.maxMana = Math.max(amount, BASE_MAX_MANA); }

    public boolean consumeMana(float amount) {
        if (mana < amount) return false;
        mana -= amount;
        return true;
    }

    public void regenMana(float amount) {
        if (isLocked()) return;
        mana = Math.min(mana + amount, maxMana);
    }

    public void lockMana(long durationMs) {
        this.lockUntil = System.currentTimeMillis() + durationMs;
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
        tag.putFloat("MaxMana", maxMana);
        tag.putLong("LockUntil", lockUntil);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.mana = tag.getFloat("Mana");
        this.maxMana = tag.getFloat("MaxMana");
        this.lockUntil = tag.getLong("LockUntil");
    }
}
