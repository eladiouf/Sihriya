package tong.sihriya.client;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import tong.sihriya.client.gui.ClientUiOptions;

public class SihriyaClientConfig {
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

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static void onLoadOrReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            ClientUiOptions.loadFromConfig();
        }
    }

    public static void saveFromOptions() {
        SHOW_MANA_HUD.set(ClientUiOptions.showManaHud);
        SHOW_ACTIVE_SPELL_HUD.set(ClientUiOptions.showActiveSpellHud);
        SHOW_NOTIFICATIONS.set(ClientUiOptions.showNotifications);
        COMPACT_HUD.set(ClientUiOptions.compactHud);
        REDUCED_MOTION.set(ClientUiOptions.reducedMotion);
        UI_SOUNDS.set(ClientUiOptions.uiSounds);
        ALTERNATE_COLORS.set(ClientUiOptions.alternateColors);
        SPEC.save();
    }
}
