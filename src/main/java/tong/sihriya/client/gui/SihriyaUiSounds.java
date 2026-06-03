package tong.sihriya.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class SihriyaUiSounds {
    private SihriyaUiSounds() {}

    public static void open() {
        play(SoundEvents.ENCHANTMENT_TABLE_USE, 0.32f, 1.45f);
    }

    public static void select() {
        play(SoundEvents.UI_BUTTON_CLICK, 1.25f);
    }

    public static void success() {
        play(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.32f, 1.35f);
    }

    public static void error() {
        play(SoundEvents.VILLAGER_NO, 0.34f, 0.7f);
    }

    public static void close() {
        play(SoundEvents.BOOK_PAGE_TURN, 0.28f, 0.9f);
    }

    private static void play(SoundEvent event, float volume, float pitch) {
        if (!ClientUiOptions.uiSounds) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, volume));
    }

    private static void play(Holder<SoundEvent> event, float pitch) {
        if (!ClientUiOptions.uiSounds) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, pitch));
    }
}
