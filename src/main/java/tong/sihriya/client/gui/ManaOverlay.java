package tong.sihriya.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import tong.sihriya.client.ClientSchoolData;
import tong.sihriya.client.ClientSchoolData;

@OnlyIn(Dist.CLIENT)
public class ManaOverlay implements IGuiOverlay {
    public static final ManaOverlay INSTANCE = new ManaOverlay();

    private static final int X = 4;
    private static final int Y = 56;
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
        float fill = maxMana > 0 ? Math.min(1f, mana / maxMana) : 0;
        boolean locked = ClientSchoolData.manaBlocked;
        boolean lowMana = fill <= 0.25f && !locked;

        int width = ClientUiOptions.compactHud ? 74 : W;

        // Ornate background
        int pulse = locked && !ClientUiOptions.reducedMotion ? (int) ((Math.sin(System.currentTimeMillis() / 180.0) + 1) * 18) : 0;
        int borderColor = locked ? (0xFF4A1010 + (pulse << 16)) : lowMana ? 0xFF4A3A10 : 0xFF1A1020;
        g.fill(X - BORDER, Y - BORDER, X + width + BORDER, Y + H + BORDER, borderColor);
        g.fill(X, Y, X + width, Y + H, 0xFF0A0A15);

        // Mana fill with gradient
        if (fill > 0) {
            int c1 = locked ? 0xFFCC3333 : lowMana ? 0xFFE0A020 : 0xFF3366FF;
            int c2 = locked ? 0xFFFF6666 : lowMana ? 0xFFFFD060 : 0xFF66AAFF;
            int filled = (int)(fill * width);
            // Draw as horizontal strips for gradient effect
            for (int i = 0; i < filled; i++) {
                float t = (float) i / width;
                int r = (int)(((c2 >> 16) & 0xFF) * t + ((c1 >> 16) & 0xFF) * (1 - t));
                int gr = (int)(((c2 >> 8) & 0xFF) * t + ((c1 >> 8) & 0xFF) * (1 - t));
                int b = (int)((c2 & 0xFF) * t + (c1 & 0xFF) * (1 - t));
                g.fill(X + i, Y + 1, X + i + 1, Y + H - 1, 0xFF000000 | (r << 16) | (gr << 8) | b);
            }
        }

        // Top highlight line
        g.fill(X + 1, Y + 1, X + width - 1, Y + 2, 0x44FFFFFF);
        drawChangeFlash(g, width);

        // Label
        Font font = mc.font;
        String active = ClientSchoolData.getActiveSchool();
        String label;
        if (locked) {
            label = I18n.get("hud.sihriya.mana_locked");
        } else if (!active.isEmpty()) {
            label = I18n.get("sihriya.school." + active);
        } else {
            label = I18n.get("sihriya.mana");
        }
        int labelColor = locked ? 0xFFFF8888 : lowMana ? 0xFFFFD060 : 0xCCCCFF;
        g.drawString(font, label, X, Y - 10, labelColor);
        String val = String.format("%.0f/%.0f", displayedMana, maxMana);
        if (!ClientUiOptions.compactHud) {
            g.drawString(font, val, X + width - font.width(val), Y - 10, 0x8888BB);
        }

        // Lock timer
        if (locked && ClientSchoolData.manaBlockRemainingMs > 0) {
            String lock = String.format("%.1fs", ClientSchoolData.manaBlockRemainingMs / 1000.0);
            g.drawString(font, lock, X + width + 4, Y, 0xFFFF4444);
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
        int alpha = (int) (90 * (1f - age / 520f));
        int color = (alpha << 24) | (changeColor & 0x00FFFFFF);
        g.fill(X, Y - 2, X + width, Y - 1, color);
        g.fill(X, Y + H + 1, X + width, Y + H + 2, color);
    }
}
