package tong.sihriya.core;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SchoolProgressionProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<SchoolProgression> SCHOOL_PROGRESSION = CapabilityManager.get(new CapabilityToken<>() {});

    private SchoolProgression progression = null;
    private final LazyOptional<SchoolProgression> lazyOptional = LazyOptional.of(this::getOrCreate);

    private SchoolProgression getOrCreate() {
        if (this.progression == null) this.progression = new SchoolProgression();
        return this.progression;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == SCHOOL_PROGRESSION ? lazyOptional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() { return getOrCreate().serializeNBT(); }

    @Override
    public void deserializeNBT(CompoundTag nbt) { getOrCreate().deserializeNBT(nbt); }
}
