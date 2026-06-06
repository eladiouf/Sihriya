package tong.sihriya.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import tong.sihriya.client.ClientSchoolData;

@OnlyIn(Dist.CLIENT)
public class ManaOverlay implements IGuiOverlay {
    public static final ManaOverlay INSTANCE = new ManaOverlay();

    private static final int X = 4;
    private static final int Y = 82;   // FIX: était 56, chevauchait les barres STAT Mod (fatigue à y=52)
    private static final int W = 100;
    private static final int H = 6;
    private static final int BORDER = 1;

    private float displayedMana = -1;
    private float previousMana = -1;
    private long changeAtMs = 0;
    private int changeColor = 0;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!ClientUiOptions.showManaHud) return;

        float mana = ClientSchoolData.mana;
        float maxMana = ClientSchoolData.maxMana;
        updateDisplayedMana(mana);   // FIX: cette méthode n'était jamais appelée → affichait -1/50
        float fill = maxMana > 0 ? Math.min(1f, mana / maxMana) : 0;
        boolean locked = ClientSchoolData.manaBlocked;
        boolean lowMana = fill <= 0.25f && !locked;

        int width = ClientUiOptions.compactHud ? 74 : W;
        long time = System.currentTimeMillis();

        String active = ClientSchoolData.getActiveSchool();
        int schoolColor = !active.isEmpty() ? SihriyaUiData.schoolColor(active) : 0xFF3366FF;

        // Halo externe : lueur colorée de l'école (épique)
        if (!locked && !lowMana && fill > 0.1f && !ClientUiOptions.reducedMotion) {
            float glowPulse = (float)(Math.sin(time / 1400.0) * 0.5 + 0.5);
            int glowAlpha = (int)(14 + glowPulse * 18);
            int glow = (glowAlpha << 24) | (schoolColor & 0x00FFFFFF);
            g.fill(X - 3, Y - 3, X + width + 3, Y + H + 3, glow);
            g.fill(X - 2, Y - 2, X + width + 2, Y + H + 2, glow);
        }

        // Bordure principale
        int pulse = locked && !ClientUiOptions.reducedMotion
            ? (int)((Math.sin(time / 180.0) + 1) * 18) : 0;
        int borderColor = locked ? (0xFF4A1010 + (Math.min(pulse, 0xFF) << 16))
            : lowMana ? 0xFF4A3A10 : 0xFF1A1020;
        g.fill(X - BORDER, Y - BORDER, X + width + BORDER, Y + H + BORDER, borderColor);

        // Fond
        g.fill(X, Y, X + width, Y + H, 0xFF0A0A15);

        // Remplissage dégradé + shimmer
        if (fill > 0) {
            int c1 = locked ? 0xFFCC3333 : lowMana ? 0xFFE0A020 : 0xFF3366FF;
            int c2 = locked ? 0xFFFF6666 : lowMana ? 0xFFFFD060 : 0xFF66AAFF;
            int filled = (int)(fill * width);

            for (int i = 0; i < filled; i++) {
                float t = (float) i / width;
                int r = lerp((c1 >> 16) & 0xFF, (c2 >> 16) & 0xFF, t);
                int gr = lerp((c1 >> 8) & 0xFF, (c2 >> 8) & 0xFF, t);
                int b = lerp(c1 & 0xFF, c2 & 0xFF, t);
                g.fill(X + i, Y + 1, X + i + 1, Y + H - 1, 0xFF000000 | (r << 16) | (gr << 8) | b);
            }

            // Shimmer : colonne brillante qui traverse la barre toutes les 2.4s (épique)
            if (!locked && !ClientUiOptions.reducedMotion) {
                float shimmerPhase = (time % 2400) / 2400.0f;
                int shimmerX = X + (int)(shimmerPhase * (filled + 16)) - 8;
                for (int i = -5; i <= 5; i++) {
                    int sx = shimmerX + i;
                    if (sx < X || sx >= X + filled) continue;
                    int shimAlpha = (int)(90 * (1f - Math.abs(i) / 6f));
                    g.fill(sx, Y + 1, sx + 1, Y + H - 1, (shimAlpha << 24) | 0xFFFFFF);
                }
            }
        }

        // Ligne de surbrillance en haut (épique)
        g.fill(X + 1, Y + 1, X + width - 1, Y + 2, 0x55FFFFFF);

        drawChangeFlash(g, width);

        // Label
        Font font = mc.font;
        String label;
        if (locked) {
            label = I18n.get("hud.sihriya.mana_locked");
        } else if (!active.isEmpty()) {
            label = I18n.get("sihriya.school." + active);
        } else {
            label = I18n.get("sihriya.mana");
        }
        int labelColor = locked ? 0xFFFF8888 : lowMana ? 0xFFFFD060 : 0xCCCCFF;
        g.drawString(font, label, X, Y - 10, labelColor, false);

        if (!ClientUiOptions.compactHud) {
            String val = String.format("%.0f/%.0f", Math.max(0, displayedMana), maxMana);
            g.drawString(font, val, X + width - font.width(val), Y - 10, 0xFF8888BB, false);
        }

        // Timer de lock
        if (locked && ClientSchoolData.manaBlockRemainingMs > 0) {
            String lock = String.format("%.1fs", ClientSchoolData.manaBlockRemainingMs / 1000.0);
            g.drawString(font, lock, X + width + 4, Y, 0xFFFF4444, false);
        }
    }

    private void updateDisplayedMana(float mana) {
        if (displayedMana < 0) {
            displayedMana = mana;
            previousMana = mana;
            return;
        }
        if (mana != previousMana) {
            changeAtMs = System.currentTimeMillis();
            changeColor = mana > previousMana ? 0xFF66FFAA : 0xFFFF6677;
            previousMana = mana;
        }
        if (ClientUiOptions.reducedMotion) {
            displayedMana = mana;
        } else {
            displayedMana += (mana - displayedMana) * 0.18f;
            if (Math.abs(displayedMana - mana) < 0.05f) displayedMana = mana;
        }
    }

    private void drawChangeFlash(GuiGraphics g, int width) {
        if (ClientUiOptions.reducedMotion || changeAtMs <= 0) return;
        long age = System.currentTimeMillis() - changeAtMs;
        if (age > 520) return;
        int alpha = (int)(90 * (1f - age / 520f));
        int color = (alpha << 24) | (changeColor & 0x00FFFFFF);
        g.fill(X, Y - 2, X + width, Y - 1, color);
        g.fill(X, Y + H + 1, X + width, Y + H + 2, color);
    }

    private static int lerp(int a, int b, float t) {
        return (int)(a + (b - a) * t);
    }
}
