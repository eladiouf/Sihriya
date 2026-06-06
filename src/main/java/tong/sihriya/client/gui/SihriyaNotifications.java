package tong.sihriya.client.gui;

import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayDeque;
import java.util.Deque;

@OnlyIn(Dist.CLIENT)
public final class SihriyaNotifications {
    private static final int MAX = 4;
    private static final long DURATION_MS = 3800L;
    private static final Deque<Entry> ENTRIES = new ArrayDeque<>();

    private SihriyaNotifications() {}

    public static void schoolUnlocked(String school) {
        push(SihriyaUiData.schoolName(school), I18n.get("notification.sihriya.school_unlocked"), SihriyaUiData.schoolColor(school));
        SihriyaUiSounds.success();
    }

    public static void spellLearned(String spellId) {
        var spell = tong.sihriya.data.SpellRegistry.get(spellId);
        if (spell == null) return;
        push(SihriyaUiData.spellName(spell), I18n.get("notification.sihriya.spell_learned"), SihriyaUiData.schoolColor(spell.school));
        SihriyaUiSounds.success();
    }

    public static void castBlocked(String school, String reasonKey) {
        push(SihriyaUiData.schoolName(school), I18n.get(reasonKey), 0xFFFF7777);
        SihriyaUiSounds.error();
    }

    public static void tierUnlocked(String school, int tier) {
        String title = SihriyaUiData.schoolName(school) + " — Tier " + tier;
        String body = I18n.get("notification.sihriya.tier_unlocked");
        push(title, body, SihriyaUiData.schoolColor(school));
        SihriyaUiSounds.success();
    }

    public static Deque<Entry> entries() {
        long now = System.currentTimeMillis();
        ENTRIES.removeIf(entry -> now - entry.createdAtMs > DURATION_MS);
        return ENTRIES;
    }

    public static float ageProgress(Entry entry) {
        return Math.max(0f, Math.min(1f, (System.currentTimeMillis() - entry.createdAtMs) / (float) DURATION_MS));
    }

    private static void push(String title, String body, int color) {
        ENTRIES.addFirst(new Entry(title, body, color, System.currentTimeMillis()));
        while (ENTRIES.size() > MAX) {
            ENTRIES.removeLast();
        }
    }

    public record Entry(String title, String body, int color, long createdAtMs) {}
}
