package tong.sihriya.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tong.sihriya.client.ClientSchoolData;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;

import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GrimoireScreen extends Screen {
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int selectedSchool = 0;
    private int selectedTier = 0;
    private int statusFilter = 0;
    private int scroll;
    private SpellData selectedSpell;

    public GrimoireScreen() {
        super(Component.translatable("screen.sihriya.grimoire"));
    }

    @Override
    protected void init() {
        panelW = Math.min(430, width - 24);
        panelH = Math.min(280, height - 24);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        selectedSchool = Math.max(0, SihriyaUiData.SCHOOL_ORDER.indexOf(ClientSchoolData.getActiveSchool()));
        scroll = 0;
        selectFirstVisibleSpell();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ensureSelectionVisible();
        renderBackground(g);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xEE17131D);
        g.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, 0xEE241B27);
        g.drawString(font, title, panelX + 12, panelY + 8, 0xFFE6D7B8, false);
        drawOptionsButton(g, mouseX, mouseY);

        drawSchoolTabs(g);
        drawSchoolSummary(g);
        drawFilters(g, mouseX, mouseY);
        drawSpellList(g, mouseX, mouseY);
        drawScrollbar(g);
        drawSpellDetails(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawSchoolTabs(GuiGraphics g) {
        int tabY = panelY + 28;
        int tabW = panelW / SihriyaUiData.SCHOOL_ORDER.size();
        for (int i = 0; i < SihriyaUiData.SCHOOL_ORDER.size(); i++) {
            String school = SihriyaUiData.SCHOOL_ORDER.get(i);
            int x = panelX + i * tabW;
            int x2 = i == SihriyaUiData.SCHOOL_ORDER.size() - 1 ? panelX + panelW : x + tabW - 1;
            boolean selected = i == selectedSchool;
            boolean unlocked = ClientSchoolData.isUnlocked(school);
            int color = selected ? SihriyaUiData.schoolColor(school) : unlocked ? 0xFF4A3D4F : 0xFF2B2830;
            g.fill(x, tabY, x2, tabY + 18, color);
            String label = SihriyaUiData.schoolName(school);
            if (font.width(label) > tabW - 4) label = label.substring(0, 1);
            int tx = x + Math.max(2, (tabW - font.width(label)) / 2);
            g.drawString(font, label, tx, tabY + 5, unlocked ? 0xFFFFFFFF : 0xFF888888, false);
        }
    }

    private void drawOptionsButton(GuiGraphics g, int mouseX, int mouseY) {
        int x = panelX + panelW - 74;
        int y = panelY + 7;
        boolean hover = mouseX >= x && mouseX <= x + 62 && mouseY >= y && mouseY <= y + 16;
        g.fill(x, y, x + 62, y + 16, hover ? 0xFF5A4964 : 0xFF3A3042);
        String text = I18n.get("screen.sihriya.options.short");
        g.drawString(font, text, x + 31 - font.width(text) / 2, y + 4, 0xFFFFFFFF, false);
    }

    private void drawSchoolSummary(GuiGraphics g) {
        String school = getSelectedSchool();
        int y = panelY + 54;
        int color = SihriyaUiData.schoolColor(school);
        g.drawString(font, SihriyaUiData.schoolName(school), panelX + 12, y, color, false);
        String level = I18n.get("screen.sihriya.grimoire.level", ClientSchoolData.getLevel(school));
        g.drawString(font, level, panelX + 92, y, 0xFFB8B0C0, false);

        if (!ClientSchoolData.isUnlocked(school)) {
            g.drawString(font, getRequirementText(school), panelX + 12, y + 13, 0xFFFFAA66, false);
        }

        int barX = panelX + panelW - 152;
        int barY = y + 2;
        int barW = 88;
        int lvl = Math.max(0, Math.min(100, ClientSchoolData.getLevel(school)));
        g.fill(barX, barY, barX + barW, barY + 5, 0xFF0B0910);
        g.fill(barX, barY, barX + (barW * lvl / 100), barY + 5, color);
        g.drawString(font, lvl + "/100", barX + barW + 6, barY - 2, 0xFFB8B0C0, false);
    }

    private void drawFilters(GuiGraphics g, int mouseX, int mouseY) {
        int tierY = panelY + 72;
        int x = panelX + 12;
        for (int i = 0; i <= 4; i++) {
            String label = i == 0 ? I18n.get("screen.sihriya.grimoire.filter_all") : "T" + i;
            int w = i == 0 ? 34 : 24;
            drawFilterButton(g, x, tierY, w, label, selectedTier == i, mouseX, mouseY);
            x += w + 4;
        }

        int statusY = tierY + 20;
        x = panelX + 12;
        for (int i = 0; i < 4; i++) {
            String label = I18n.get("screen.sihriya.grimoire.status_" + i);
            int w = Math.max(42, font.width(label) + 10);
            drawFilterButton(g, x, statusY, w, label, statusFilter == i, mouseX, mouseY);
            x += w + 4;
        }
    }

    private void drawFilterButton(GuiGraphics g, int x, int y, int w, String label, boolean selected, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 15;
        int bg = selected ? 0xFF5A4964 : hover ? 0xFF3A3042 : 0xAA0B0910;
        g.fill(x, y, x + w, y + 15, bg);
        g.drawString(font, label, x + w / 2 - font.width(label) / 2, y + 4, selected ? 0xFFFFFFFF : 0xFFB8B0C0, false);
    }

    private void drawSpellList(GuiGraphics g, int mouseX, int mouseY) {
        int listX = panelX + 12;
        int listY = getListY();
        int listW = 150;
        int rowH = 22;
        List<SpellData> spells = getVisibleSpells();

        g.fill(listX - 4, listY - 4, listX + listW, panelY + panelH - 12, 0xAA0B0910);
        int maxRows = Math.max(1, (panelY + panelH - 16 - listY) / rowH);
        int start = Math.max(0, Math.min(scroll, Math.max(0, spells.size() - maxRows)));

        for (int i = 0; i < maxRows && start + i < spells.size(); i++) {
            SpellData spell = spells.get(start + i);
            int y = listY + i * rowH;
            boolean selected = selectedSpell != null && selectedSpell.id.equals(spell.id);
            boolean learned = ClientSchoolData.isSpellLearned(spell.id);
            boolean castable = ClientSchoolData.canCast(spell);
            int bg = selected ? 0x663366FF : mouseX >= listX && mouseX <= listX + listW && mouseY >= y && mouseY < y + rowH ? 0x33333333 : 0;
            if (bg != 0) g.fill(listX - 2, y - 1, listX + listW - 2, y + rowH - 1, bg);

            SpellIconRenderer.renderIconScaled(g, spell, listX, y + 2, 16);
            int textColor = castable ? 0xFFFFFFFF : learned ? 0xFFB8B0C0 : 0xFF777777;
            g.drawString(font, SihriyaUiData.spellName(spell), listX + 20, y + 2, textColor, false);
            g.drawString(font, "T" + spell.tier, listX + 20, y + 12, 0xFF8D8496, false);
        }
    }

    private void drawScrollbar(GuiGraphics g) {
        List<SpellData> spells = getVisibleSpells();
        int listX = panelX + 12;
        int listY = getListY();
        int listW = 150;
        int rowH = 22;
        int maxRows = Math.max(1, (panelY + panelH - 16 - listY) / rowH);
        int totalRows = spells.size();
        if (totalRows <= maxRows) return;

        int barX = listX + listW - 4;
        int barY = listY;
        int barH = maxRows * rowH;
        int handleH = Math.max(16, barH * maxRows / totalRows);
        int maxScroll = Math.max(0, totalRows - maxRows);
        int handleY = barY + (maxScroll > 0 ? (barH - handleH) * scroll / maxScroll : 0);

        g.fill(barX - 1, barY - 1, barX + 3, barY + barH + 1, 0xFF2B2830);
        g.fill(barX, handleY, barX + 2, handleY + handleH, 0xFF5A4964);
    }

    private void drawSpellDetails(GuiGraphics g) {
        int x = panelX + 176;
        int y = getListY();
        int w = panelW - 188;
        g.fill(x - 4, y - 4, panelX + panelW - 12, panelY + panelH - 12, 0xAA0B0910);

        if (selectedSpell == null) {
            g.drawString(font, I18n.get("screen.sihriya.grimoire.no_spells"), x, y, 0xFFAAAAAA, false);
            return;
        }

        SpellIconRenderer.renderIconScaled(g, selectedSpell, x, y, 32);
        g.drawString(font, SihriyaUiData.spellName(selectedSpell), x + 38, y + 2, 0xFFFFFFFF, false);
        g.drawString(font, SihriyaUiData.spellTypeName(selectedSpell), x + 38, y + 14, 0xFFB8B0C0, false);

        int line = y + 44;
        drawDetail(g, x, line, I18n.get("screen.sihriya.grimoire.tier"), "T" + selectedSpell.tier); line += 12;
        drawDetail(g, x, line, I18n.get("screen.sihriya.grimoire.mana"), String.valueOf(selectedSpell.manaCost)); line += 12;
        drawDetail(g, x, line, I18n.get("screen.sihriya.grimoire.cooldown"), String.format("%.1fs", selectedSpell.cooldown / 20.0f)); line += 16;

        boolean learned = ClientSchoolData.isSpellLearned(selectedSpell.id);
        boolean castable = ClientSchoolData.canCast(selectedSpell);
        String status = castable
            ? I18n.get("screen.sihriya.grimoire.castable")
            : learned ? I18n.get("screen.sihriya.grimoire.learned")
            : I18n.get("screen.sihriya.grimoire.locked");
        g.drawString(font, status, x, line, castable ? 0xFF90EE90 : learned ? 0xFFFFD060 : 0xFFFF7777, false);
        line += 16;

        line = drawWrapped(g, SihriyaUiData.spellDescription(selectedSpell), x, line, w, 0xFFD8D0E0, 3);
        line += 4;

        g.drawString(font, I18n.get("screen.sihriya.grimoire.effects"), x, line, 0xFFE6D7B8, false);
        line += 12;
        for (var effect : selectedSpell.effects) {
            if (line > panelY + panelH - 22) break;
            String effectText = SihriyaUiData.effectName(effect.type) + " " + trim(effect.baseValue) + " +" + trim(effect.scaling) + "/lvl";
            if (effect.duration > 0) effectText += " " + String.format("%.1fs", effect.duration / 20.0f);
            if (font.width(effectText) > w) effectText = font.plainSubstrByWidth(effectText, w);
            g.drawString(font, effectText, x, line, 0xFFB8B0C0, false);
            line += 11;
        }
    }

    private int drawWrapped(GuiGraphics g, String text, int x, int y, int width, int color, int maxLines) {
        String remaining = text;
        int line = 0;
        while (!remaining.isEmpty() && line < maxLines) {
            String part = font.plainSubstrByWidth(remaining, width);
            if (part.isEmpty()) break;
            int consumed = part.length();
            if (consumed < remaining.length()) {
                int lastSpace = part.lastIndexOf(' ');
                if (lastSpace > 8) {
                    part = part.substring(0, lastSpace);
                    consumed = lastSpace + 1;
                }
            }
            if (line == maxLines - 1 && consumed < remaining.length()) {
                part = font.plainSubstrByWidth(part, width - font.width("...")) + "...";
            }
            g.drawString(font, part, x, y + line * 11, color, false);
            remaining = remaining.substring(Math.min(consumed, remaining.length())).stripLeading();
            line++;
        }
        return y + line * 11;
    }

    private void drawDetail(GuiGraphics g, int x, int y, String label, String value) {
        g.drawString(font, label + ": ", x, y, 0xFF8D8496, false);
        g.drawString(font, value, x + 70, y, 0xFFD8D0E0, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int tabY = panelY + 28;
        int tabW = panelW / SihriyaUiData.SCHOOL_ORDER.size();
        int optionX = panelX + panelW - 74;
        int optionY = panelY + 7;
        if (mouseX >= optionX && mouseX <= optionX + 62 && mouseY >= optionY && mouseY <= optionY + 16) {
            SihriyaUiSounds.open();
            minecraft.setScreen(new SihriyaOptionsScreen(this));
            return true;
        }

        if (mouseY >= tabY && mouseY <= tabY + 18 && mouseX >= panelX && mouseX <= panelX + panelW) {
            selectedSchool = Math.max(0, Math.min(SihriyaUiData.SCHOOL_ORDER.size() - 1, (int) ((mouseX - panelX) / tabW)));
            scroll = 0;
            selectFirstVisibleSpell();
            SihriyaUiSounds.select();
            return true;
        }

        if (handleFilterClick(mouseX, mouseY)) {
            scroll = 0;
            selectFirstVisibleSpell();
            SihriyaUiSounds.select();
            return true;
        }

        int listX = panelX + 12;
        int listY = getListY();
        int rowH = 22;
        int maxRows = Math.max(1, (panelY + panelH - 16 - listY) / rowH);
        if (mouseX >= listX && mouseX <= listX + 150 && mouseY >= listY && mouseY <= panelY + panelH - 16) {
            int index = scroll + (int) ((mouseY - listY) / rowH);
            List<SpellData> spells = getVisibleSpells();
            if (index >= 0 && index < spells.size() && index < scroll + maxRows) {
                selectedSpell = spells.get(index);
                SihriyaUiSounds.select();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<SpellData> spells = getVisibleSpells();
        int maxRows = Math.max(1, (panelY + panelH - 16 - getListY()) / 22);
        int maxScroll = Math.max(0, spells.size() - maxRows);
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(delta)));
        return true;
    }

    private List<SpellData> getVisibleSpells() {
        return SpellRegistry.getBySchool(getSelectedSchool()).stream()
            .filter(spell -> selectedTier == 0 || spell.tier == selectedTier)
            .filter(this::matchesStatusFilter)
            .sorted(Comparator.comparingInt((SpellData spell) -> spell.tier).thenComparing(spell -> spell.id))
            .toList();
    }

    private boolean matchesStatusFilter(SpellData spell) {
        boolean learned = ClientSchoolData.isSpellLearned(spell.id);
        boolean castable = ClientSchoolData.canCast(spell);
        return switch (statusFilter) {
            case 1 -> castable;
            case 2 -> learned;
            case 3 -> !castable;
            default -> true;
        };
    }

    private boolean handleFilterClick(double mouseX, double mouseY) {
        int tierY = panelY + 72;
        int x = panelX + 12;
        for (int i = 0; i <= 4; i++) {
            int w = i == 0 ? 34 : 24;
            if (mouseX >= x && mouseX <= x + w && mouseY >= tierY && mouseY <= tierY + 15) {
                selectedTier = i;
                return true;
            }
            x += w + 4;
        }

        int statusY = tierY + 20;
        x = panelX + 12;
        for (int i = 0; i < 4; i++) {
            String label = I18n.get("screen.sihriya.grimoire.status_" + i);
            int w = Math.max(42, font.width(label) + 10);
            if (mouseX >= x && mouseX <= x + w && mouseY >= statusY && mouseY <= statusY + 15) {
                statusFilter = i;
                return true;
            }
            x += w + 4;
        }
        return false;
    }

    private int getListY() {
        return panelY + 116;
    }

    private void selectFirstVisibleSpell() {
        selectedSpell = getVisibleSpells().stream().findFirst().orElse(null);
    }

    private void ensureSelectionVisible() {
        if (selectedSpell == null) return;
        boolean stillVisible = getVisibleSpells().stream().anyMatch(spell -> spell.id.equals(selectedSpell.id));
        if (!stillVisible) selectFirstVisibleSpell();
    }

    private String getSelectedSchool() {
        return SihriyaUiData.SCHOOL_ORDER.get(selectedSchool);
    }

    private String getRequirementText(String school) {
        return I18n.get("screen.sihriya.grimoire.requires") + " "
            + SihriyaUiData.unlockRequirementText(school, I18n.get("screen.sihriya.grimoire.locked"));
    }

    private String trim(float value) {
        if (value == (int) value) return String.valueOf((int) value);
        return String.format("%.2f", value);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
