package tong.sihriya.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import tong.sihriya.client.ClientSchoolData;
import tong.sihriya.client.vfx.render.ProceduralTextureHelper;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.sihriya.network.HotbarUpdatePacket;
import tong.sihriya.network.NetworkHandler;

import java.util.ArrayList;
import java.util.List;

public class HotbarScreen extends Screen {
    private static final int SLOTS = 6;
    private final List<SpellData>[] schoolSpells = new ArrayList[9];
    private int selectedSchool = 0;
    private int scroll = 0;

    private static final String[] SCHOOLS = {
        "fire", "water", "wind", "earth",
        "lightning", "ice", "lava", "necromancy", "lumamancy"
    };

    public HotbarScreen() {
        super(Component.literal("Spell Hotbar"));
        for (int i = 0; i < SCHOOLS.length; i++) {
            schoolSpells[i] = new ArrayList<>(SpellRegistry.getBySchool(SCHOOLS[i]));
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cw = width / 2;
        int ch = height / 2;

        // Title
        g.drawString(font, "Spell Hotbar — 1-6 to assign, ESC to save", cw - 100, ch - 90, 0xFFD4FF00, false);

        // Top: 6 hotbar slots
        int slotY = ch - 70;
        for (int i = 0; i < SLOTS; i++) {
            int sx = cw - 90 + i * 32;
            String spellId = ClientSchoolData.getHotbarSpell(i);
            SpellData spell = SpellRegistry.get(spellId);
            g.fill(sx, slotY, sx + 28, slotY + 28, spell != null ? 0xEE444444 : 0x66222222);
            g.drawString(font, String.valueOf(i + 1), sx + 10, slotY + 8, 0xFFD4FF00, false);
            if (spell != null) {
                SpellIconRenderer.renderIconScaled(g, spell, sx, slotY, 16);
                g.drawString(font, SihriyaUiData.spellName(spell), sx + 20, slotY + 6, 0xFFFFFFFF, false);
            }
        }

        // School tabs
        int tabY = ch - 30;
        int tabSize = Math.min(60, (width - 40) / SCHOOLS.length);
        for (int i = 0; i < SCHOOLS.length; i++) {
            int tx = 20 + i * tabSize;
            boolean hover = mx >= tx && mx <= tx + tabSize - 2 && my >= tabY && my <= tabY + 18;
            g.fill(tx, tabY, tx + tabSize - 2, tabY + 18, i == selectedSchool ? 0xAA444444 : hover ? 0x66333333 : 0x44222222);
            g.drawString(font, SCHOOLS[i].substring(0, Math.min(4, SCHOOLS[i].length())), tx + 4, tabY + 4, i == selectedSchool ? 0xFFD4FF00 : 0xFF888888, false);
        }

        // Spell list for selected school
        int listX = 20;
        int listY = tabY + 25;
        var spells = schoolSpells[selectedSchool];
        for (int idx = scroll; idx < Math.min(spells.size(), scroll + 8); idx++) {
            var spell = spells.get(idx);
            boolean learned = ClientSchoolData.isSpellLearned(spell.id);
            boolean hover = mx >= listX && mx <= listX + width - 40 && my >= listY && my <= listY + 20;
            g.fill(listX, listY, listX + width - 40, listY + 20, hover ? 0x66333333 : 0x22111111);
            SpellIconRenderer.renderIconScaled(g, spell, listX + 2, listY + 2, 16);
            g.drawString(font, SihriyaUiData.spellName(spell), listX + 22, listY + 5, learned ? 0xFFFFFFFF : 0xFF888888, false);
            if (learned) {
                g.drawString(font, "T" + spell.tier, listX + width - 90, listY + 5, 0xFF888888, false);
                g.drawString(font, spell.manaCost + "MP", listX + width - 60, listY + 5, 0xFF88FF88, false);
            } else {
                g.drawString(font, "Locked", listX + width - 60, listY + 5, 0xFFFF8888, false);
            }
            listY += 22;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn != 0) return super.mouseClicked(mx, my, btn);
        int cw = width / 2;

        // School tabs
        int tabY = height / 2 - 30;
        int tabSize = Math.min(60, (width - 40) / SCHOOLS.length);
        for (int i = 0; i < SCHOOLS.length; i++) {
            int tx = 20 + i * tabSize;
            if (mx >= tx && mx <= tx + tabSize - 2 && my >= tabY && my <= tabY + 18) {
                selectedSchool = i;
                scroll = 0;
                return true;
            }
        }

        // Spell list click → bind to selected slot
        int listX = 20;
        int listY = tabY + 25;
        var spells = schoolSpells[selectedSchool];
        for (int idx = scroll; idx < Math.min(spells.size(), scroll + 8); idx++) {
            var spell = spells.get(idx);
            if (mx >= listX && mx <= listX + width - 40 && my >= listY && my <= listY + 20) {
                if (ClientSchoolData.isSpellLearned(spell.id)) {
                    // Find first empty hotbar slot
                    int slot = -1;
                    for (int s = 0; s < SLOTS; s++) {
                        if (ClientSchoolData.getHotbarSpell(s) == null || ClientSchoolData.getHotbarSpell(s).isEmpty()) {
                            slot = s;
                            break;
                        }
                    }
                    if (slot >= 0) {
                        ClientSchoolData.setHotbarSpell(slot, spell.id);
                        NetworkHandler.CHANNEL.sendToServer(new HotbarUpdatePacket(slot, spell.id));
                    }
                }
                return true;
            }
            listY += 22;
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean keyPressed(int key, int sc, int mod) {
        if (key == 256) { onClose(); return true; }
        return super.keyPressed(key, sc, mod);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        var spells = schoolSpells[selectedSchool];
        int maxScroll = Math.max(0, spells.size() - 8);
        scroll = (int) Math.max(0, Math.min(maxScroll, scroll - delta));
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
