package tong.sihriya.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import tong.sihriya.client.ClientManaData;
import tong.sihriya.client.ClientSchoolData;

@OnlyIn(Dist.CLIENT)
public class ManaOverlay implements IGuiOverlay {
    public static final ManaOverlay INSTANCE = new ManaOverlay();

    private static final int X = 4;
    private static final int Y = 56;
    private static final int W = 100;
    private static final int H = 6;
    private static final int BORDER = 1;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        float mana = ClientManaData.mana;
        float maxMana = ClientManaData.maxMana;
        float fill = maxMana > 0 ? Math.min(1f, mana / maxMana) : 0;
        boolean locked = ClientManaData.locked;

        // Ornate background
        g.fill(X - BORDER, Y - BORDER, X + W + BORDER, Y + H + BORDER, 0xFF1A1020);
        g.fill(X, Y, X + W, Y + H, 0xFF0A0A15);

        // Mana fill with gradient
        if (fill > 0) {
            int c1 = locked ? 0xFFCC3333 : 0xFF3366FF;
            int c2 = locked ? 0xFFFF6666 : 0xFF66AAFF;
            int filled = (int)(fill * W);
            // Draw as horizontal strips for gradient effect
            for (int i = 0; i < filled; i++) {
                float t = (float) i / W;
                int r = (int)(((c2 >> 16) & 0xFF) * t + ((c1 >> 16) & 0xFF) * (1 - t));
                int gr = (int)(((c2 >> 8) & 0xFF) * t + ((c1 >> 8) & 0xFF) * (1 - t));
                int b = (int)((c2 & 0xFF) * t + (c1 & 0xFF) * (1 - t));
                g.fill(X + i, Y + 1, X + i + 1, Y + H - 1, 0xFF000000 | (r << 16) | (gr << 8) | b);
            }
        }

        // Top highlight line
        g.fill(X + 1, Y + 1, X + W - 1, Y + 2, 0x44FFFFFF);

        // Label
        Font font = mc.font;
        String active = ClientSchoolData.activeSchool;
        String label;
        if (locked) {
            label = "Mana [Bloque]";
        } else if (!active.isEmpty()) {
            label = active.substring(0, 1).toUpperCase() + active.substring(1);
        } else {
            label = "Mana";
        }
        g.drawString(font, label, X, Y - 10, 0xCCCCFF);
        String val = String.format("%.0f/%.0f", mana, maxMana);
        g.drawString(font, val, X + W - font.width(val), Y - 10, 0x8888BB);

        // Lock timer
        if (locked && ClientManaData.lockRemaining > 0) {
            String lock = String.format("%.1fs", ClientManaData.lockRemaining / 1000.0);
            g.drawString(font, lock, X + W + 4, Y, 0xFFFF4444);
        }
    }
}