package tong.sihriya.animation;

import net.minecraft.resources.ResourceLocation;

public record SpellAnimation(
    ResourceLocation id,
    String spellId,
    int durationTicks,
    String particleType
) {}
