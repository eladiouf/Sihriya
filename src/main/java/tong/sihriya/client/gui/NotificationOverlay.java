package tong.sihriya.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class NotificationOverlay implements IGuiOverlay {
    public static final NotificationOverlay INSTANCE = new NotificationOverlay();

    private static final int W = 172;
    private static final int H = 34;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!ClientUiOptions.showNotifications) return;

        Font font = mc.font;
        int index = 0;
        for (var entry : SihriyaNotifications.entries()) {
            float age = SihriyaNotifications.ageProgress(entry);
            float fade = ClientUiOptions.reducedMotion ? 1f : age < 0.12f ? age / 0.12f : age > 0.82f ? (1f - age) / 0.18f : 1f;
            fade = Math.max(0f, Math.min(1f, fade));
            int alpha = (int) (fade * 220);
            if (alpha <= 0) continue;

            int slide = ClientUiOptions.reducedMotion ? 0 : (int) ((1f - fade) * 18);
            int x = sw - W - 10 + slide;
            int y = 18 + index * (H + 6);
            int bg = (alpha << 24) | 0x15101B;
            int inner = ((int) (fade * 170) << 24) | 0x241B27;
            int color = applyAlpha(entry.color(), alpha);

            g.fill(x, y, x + W, y + H, bg);
            g.fill(x + 1, y + 1, x + W - 1, y + H - 1, inner);
            g.fill(x, y, x + 3, y + H, color);

            String title = fit(font, entry.title(), W - 18);
            String body = fit(font, entry.body(), W - 18);
            g.drawString(font, title, x + 10, y + 7, applyAlpha(0xFFFFFFFF, alpha), false);
            g.drawString(font, body, x + 10, y + 19, applyAlpha(0xFFB8B0C0, alpha), false);
            index++;
        }
    }

    private int applyAlpha(int argb, int alpha) {
        return (alpha << 24) | (argb & 0x00FFFFFF);
    }

    private String fit(Font font, String text, int width) {
        if (font.width(text) <= width) return text;
        return font.plainSubstrByWidth(text, width - font.width("...")) + "...";
    }
}
