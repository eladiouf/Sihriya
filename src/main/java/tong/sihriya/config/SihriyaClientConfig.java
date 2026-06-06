package tong.sihriya.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class SihriyaClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue SHOW_MANA_HUD = BUILDER
        .comment("Show the Sihriya mana HUD.")
        .define("ui.showManaHud", true);
    public static final ForgeConfigSpec.BooleanValue SHOW_ACTIVE_SPELL_HUD = BUILDER
        .comment("Show the active spell/cooldown HUD.")
        .define("ui.showActiveSpellHud", true);
    public static final ForgeConfigSpec.BooleanValue SHOW_NOTIFICATIONS = BUILDER
        .comment("Show Sihriya unlock and cast feedback notifications.")
        .define("ui.showNotifications", true);
    public static final ForgeConfigSpec.BooleanValue COMPACT_HUD = BUILDER
        .comment("Use compact HUD panels.")
        .define("ui.compactHud", false);
    public static final ForgeConfigSpec.BooleanValue REDUCED_MOTION = BUILDER
        .comment("Reduce UI animations.")
        .define("ui.reducedMotion", false);
    public static final ForgeConfigSpec.BooleanValue UI_SOUNDS = BUILDER
        .comment("Play Sihriya UI feedback sounds.")
        .define("ui.uiSounds", true);
    public static final ForgeConfigSpec.BooleanValue ALTERNATE_COLORS = BUILDER
        .comment("Use alternate high-distinction school colors.")
        .define("ui.alternateColors", false);

    public static final ForgeConfigSpec.BooleanValue VFX_BLOOM = BUILDER
        .comment("Enable bloom post-processing for spell effects")
        .define("vfx.bloom", true);
    public static final ForgeConfigSpec.DoubleValue VFX_BLOOM_INTENSITY = BUILDER
        .comment("Bloom intensity (0.0 - 2.0)")
        .defineInRange("vfx.bloomIntensity", 0.8, 0.0, 2.0);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private SihriyaClientConfig() {
    }

    public static void saveUiOptions(boolean showManaHud, boolean showActiveSpellHud,
                                     boolean showNotifications, boolean compactHud,
                                     boolean reducedMotion, boolean uiSounds,
                                     boolean alternateColors) {
        SHOW_MANA_HUD.set(showManaHud);
        SHOW_ACTIVE_SPELL_HUD.set(showActiveSpellHud);
        SHOW_NOTIFICATIONS.set(showNotifications);
        COMPACT_HUD.set(compactHud);
        REDUCED_MOTION.set(reducedMotion);
        UI_SOUNDS.set(uiSounds);
        ALTERNATE_COLORS.set(alternateColors);
        SPEC.save();
    }
}
