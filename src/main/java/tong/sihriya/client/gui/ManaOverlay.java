package tong.sihriya.client.gui;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.client.ClientManaData;

public class ManaOverlay implements IGuiOverlay {
    public static final ManaOverlay INSTANCE = new ManaOverlay();

    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 4;
    private static final int BAR_X = 4;
    private static final int BAR_Y = 56;
    private static final int MANA_COLOR = 0xFF3399FF;
    private static final int MANA_BG_COLOR = 0xFF1A1A2E;
    private static final int LOCK_COLOR = 0xFFFF4444;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        float mana = ClientManaData.mana;
        float maxMana = ClientManaData.maxMana;
        float fill = maxMana > 0 ? mana / maxMana : 0;
        boolean locked = ClientManaData.locked;

        // Background
        graphics.fill(BAR_X, BAR_Y, BAR_X + BAR_WIDTH, BAR_Y + BAR_HEIGHT, MANA_BG_COLOR);

        // Mana fill
        int filledW = (int)(fill * BAR_WIDTH);
        if (filledW > 0) {
            int color = locked ? LOCK_COLOR : MANA_COLOR;
            graphics.fill(BAR_X, BAR_Y, BAR_X + filledW, BAR_Y + BAR_HEIGHT, color);
        }

        // Label
        var font = mc.font;
        String text = locked ? "MANA [LOCKED]" : String.format("Mana: %.0f/%.0f", mana, maxMana);
        graphics.drawString(font, text, BAR_X, BAR_Y - 10, 0xFFFFFF);

        // Lock overlay
        if (locked) {
            String lockText = String.format("%.1fs", ClientManaData.lockRemaining / 1000.0);
            graphics.drawString(font, lockText, BAR_X + BAR_WIDTH + 4, BAR_Y, LOCK_COLOR);
        }
    }
}
