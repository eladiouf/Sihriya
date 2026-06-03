package tong.sihriya.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SihriyaOptionsScreen extends Screen {
    private static final int OPTION_COUNT = 7;
    private static final int ROW_H = 24;

    private final Screen parent;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public SihriyaOptionsScreen(Screen parent) {
        super(Component.translatable("screen.sihriya.options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelW = Math.min(330, width - 24);
        panelH = 226;
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xEE17131D);
        g.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 0xEE241B27);
        g.drawString(font, title, panelX + 12, panelY + 10, 0xFFE6D7B8, false);

        int y = panelY + 34;
        for (int i = 0; i < OPTION_COUNT; i++) {
            drawOption(g, i, panelX + 12, y + i * ROW_H, mouseX, mouseY);
        }

        int doneY = panelY + panelH - 28;
        boolean hoverDone = mouseX >= panelX + panelW - 78 && mouseX <= panelX + panelW - 12
            && mouseY >= doneY && mouseY <= doneY + 18;
        g.fill(panelX + panelW - 78, doneY, panelX + panelW - 12, doneY + 18, hoverDone ? 0xFF5A4964 : 0xFF3A3042);
        String done = I18n.get("screen.sihriya.options.done");
        g.drawString(font, done, panelX + panelW - 45 - font.width(done) / 2, doneY + 5, 0xFFFFFFFF, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawOption(GuiGraphics g, int index, int x, int y, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= panelX + panelW - 12 && mouseY >= y && mouseY <= y + 18;
        boolean enabled = ClientUiOptions.get(index);
        g.fill(x, y, panelX + panelW - 12, y + 18, hover ? 0x553A3042 : 0x330B0910);
        g.drawString(font, I18n.get("screen.sihriya.options." + index), x + 8, y + 5, 0xFFD8D0E0, false);

        int toggleX = panelX + panelW - 58;
        int color = enabled ? 0xFF4CAF78 : 0xFF6A3A44;
        g.fill(toggleX, y + 3, toggleX + 38, y + 15, 0xFF0B0910);
        g.fill(toggleX + (enabled ? 18 : 2), y + 4, toggleX + (enabled ? 36 : 20), y + 14, color);
        String state = I18n.get(enabled ? "screen.sihriya.options.on" : "screen.sihriya.options.off");
        g.drawString(font, state, toggleX - font.width(state) - 6, y + 5, enabled ? 0xFF90EE90 : 0xFFFF9999, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int y = panelY + 34;
        for (int i = 0; i < OPTION_COUNT; i++) {
            int rowY = y + i * ROW_H;
            if (mouseX >= panelX + 12 && mouseX <= panelX + panelW - 12 && mouseY >= rowY && mouseY <= rowY + 18) {
                ClientUiOptions.toggle(i);
                SihriyaUiSounds.select();
                return true;
            }
        }

        int doneY = panelY + panelH - 28;
        if (mouseX >= panelX + panelW - 78 && mouseX <= panelX + panelW - 12 && mouseY >= doneY && mouseY <= doneY + 18) {
            SihriyaUiSounds.close();
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
