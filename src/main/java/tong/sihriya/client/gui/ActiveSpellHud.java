package tong.sihriya.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import tong.sihriya.client.ClientManaData;
import tong.sihriya.client.ClientSchoolData;
import tong.sihriya.data.SpellRegistry.SpellData;

@OnlyIn(Dist.CLIENT)
public class ActiveSpellHud implements IGuiOverlay {
    public static final ActiveSpellHud INSTANCE = new ActiveSpellHud();

    private static final int ICON = 32;
    private static final int PAD = 6;
    private static final int W = 150;
    private static final int H = 48;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (!ClientUiOptions.showActiveSpellHud) return;

        SpellData spell = getHudSpell();
        if (spell == null) return;

        boolean coolingDown = ClientSchoolData.hasVisibleCastHud();
        float progress = coolingDown ? ClientSchoolData.getLastRequestedCooldownProgress() : 1f;
        boolean enoughMana = ClientManaData.mana >= spell.manaCost && !ClientManaData.locked;

        int width = ClientUiOptions.compactHud ? 50 : W;
        int x = sw - width - 10;
        int y = sh - 76;
        int schoolColor = SihriyaUiData.schoolColor(spell.school);

        g.fill(x, y, x + width, y + H, 0xAA0B0910);
        g.fill(x + 1, y + 1, x + width - 1, y + H - 1, 0xAA211A28);
        g.fill(x, y, x + 3, y + H, schoolColor);

        int iconX = x + PAD + 2;
        int iconY = y + PAD + 2;
        SpellIconRenderer.renderIconScaled(g, spell, iconX, iconY, ICON);
        drawCooldownTicks(g, iconX - 2, iconY - 2, ICON + 4, progress, schoolColor, coolingDown);

        if (ClientUiOptions.compactHud) return;

        Font font = mc.font;
        String school = SihriyaUiData.schoolName(spell.school);
        String name = fit(font, SihriyaUiData.spellName(spell), 88);
        g.drawString(font, school, x + 46, y + 8, schoolColor, false);
        g.drawString(font, name, x + 46, y + 20, 0xFFFFFFFF, false);

        String meta = coolingDown
            ? I18n.get("hud.sihriya.cooldown", Math.max(0.0f, (1f - progress) * spell.cooldown / 20.0f))
            : I18n.get("hud.sihriya.ready");
        int metaColor = coolingDown ? 0xFFFFD060 : enoughMana ? 0xFF90EE90 : 0xFFFF7777;
        g.drawString(font, meta, x + 46, y + 32, metaColor, false);

        String mana = spell.manaCost + " " + I18n.get("sihriya.mana");
        g.drawString(font, mana, x + width - PAD - font.width(mana), y + 32, enoughMana ? 0xFFB8D6FF : 0xFFFF7777, false);
    }

    private SpellData getHudSpell() {
        if (ClientSchoolData.hasVisibleCastHud()) {
            return ClientSchoolData.getLastRequestedSpell().orElse(null);
        }
        if (!ClientSchoolData.activeSchool.isEmpty()) {
            return ClientSchoolData.getBestCastableSpell(ClientSchoolData.activeSchool).orElse(null);
        }
        return null;
    }

    private void drawCooldownTicks(GuiGraphics g, int x, int y, int size, float progress, int color, boolean coolingDown) {
        int bg = 0xAA000000;
        g.fill(x, y, x + size, y + 2, bg);
        g.fill(x, y + size - 2, x + size, y + size, bg);
        g.fill(x, y, x + 2, y + size, bg);
        g.fill(x + size - 2, y, x + size, y + size, bg);

        int ticks = 16;
        int lit = coolingDown ? Math.round(ticks * progress) : ticks;
        for (int i = 0; i < ticks; i++) {
            int tickColor = i < lit ? color : 0x66333333;
            int tx;
            int ty;
            if (i < 4) {
                tx = x + 3 + i * (size - 6) / 4;
                ty = y;
            } else if (i < 8) {
                tx = x + size - 2;
                ty = y + 3 + (i - 4) * (size - 6) / 4;
            } else if (i < 12) {
                tx = x + size - 6 - (i - 8) * (size - 6) / 4;
                ty = y + size - 2;
            } else {
                tx = x;
                ty = y + size - 6 - (i - 12) * (size - 6) / 4;
            }
            g.fill(tx, ty, tx + 3, ty + 3, tickColor);
        }
    }

    private String fit(Font font, String text, int width) {
        if (font.width(text) <= width) return text;
        return font.plainSubstrByWidth(text, width - font.width("...")) + "...";
    }
}
