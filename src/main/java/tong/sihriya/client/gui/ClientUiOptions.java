package tong.sihriya.client.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tong.sihriya.client.SihriyaClientConfig;

@OnlyIn(Dist.CLIENT)
public final class ClientUiOptions {
    public static boolean showManaHud = true;
    public static boolean showActiveSpellHud = true;
    public static boolean showNotifications = true;
    public static boolean compactHud = false;
    public static boolean reducedMotion = false;
    public static boolean uiSounds = true;
    public static boolean alternateColors = false;

    private ClientUiOptions() {}

    public static boolean get(int index) {
        return switch (index) {
            case 0 -> showManaHud;
            case 1 -> showActiveSpellHud;
            case 2 -> showNotifications;
            case 3 -> compactHud;
            case 4 -> reducedMotion;
            case 5 -> uiSounds;
            case 6 -> alternateColors;
            default -> false;
        };
    }

    public static void toggle(int index) {
        switch (index) {
            case 0 -> showManaHud = !showManaHud;
            case 1 -> showActiveSpellHud = !showActiveSpellHud;
            case 2 -> showNotifications = !showNotifications;
            case 3 -> compactHud = !compactHud;
            case 4 -> reducedMotion = !reducedMotion;
            case 5 -> uiSounds = !uiSounds;
            case 6 -> alternateColors = !alternateColors;
            default -> {}
        }
        SihriyaClientConfig.saveFromOptions();
    }

    public static void loadFromConfig() {
        showManaHud = SihriyaClientConfig.SHOW_MANA_HUD.get();
        showActiveSpellHud = SihriyaClientConfig.SHOW_ACTIVE_SPELL_HUD.get();
        showNotifications = SihriyaClientConfig.SHOW_NOTIFICATIONS.get();
        compactHud = SihriyaClientConfig.COMPACT_HUD.get();
        reducedMotion = SihriyaClientConfig.REDUCED_MOTION.get();
        uiSounds = SihriyaClientConfig.UI_SOUNDS.get();
        alternateColors = SihriyaClientConfig.ALTERNATE_COLORS.get();
    }
}
