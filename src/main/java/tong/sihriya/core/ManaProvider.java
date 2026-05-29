package tong.sihriya.core;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManaProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<ManaManager> MANA = CapabilityManager.get(new CapabilityToken<>() {});

    private ManaManager manaManager = null;
    private final LazyOptional<ManaManager> lazyOptional = LazyOptional.of(this::getOrCreate);

    private ManaManager getOrCreate() {
        if (this.manaManager == null) this.manaManager = new ManaManager();
        return this.manaManager;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == MANA ? lazyOptional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return getOrCreate().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreate().deserializeNBT(nbt);
    }
}
