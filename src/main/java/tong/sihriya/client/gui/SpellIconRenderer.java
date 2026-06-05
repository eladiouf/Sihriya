package tong.sihriya.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;

import java.util.*;

public class SpellIconRenderer {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(Sihriya.MODID, "gui/spell_icons.png");
    public static final int ICON_SIZE = 32;
    public static final int COLS = 28;
    public static final int ROWS = 9;

    private static final List<String> SCHOOL_ORDER = Arrays.asList(
        "fire", "water", "wind", "earth",
        "lightning", "ice", "lava", "necromancy", "lumamancy"
    );

    private static final Map<String, Integer> COLUMN_CACHE = new HashMap<>();

    public static void init() {
        COLUMN_CACHE.clear();
        for (String school : SCHOOL_ORDER) {
            List<SpellData> schoolSpells = SpellRegistry.getBySchool(school).stream()
                .sorted(Comparator.comparingInt((SpellData s) -> s.tier).thenComparing(s -> s.id))
                .toList();
            for (int i = 0; i < schoolSpells.size(); i++) {
                COLUMN_CACHE.put(schoolSpells.get(i).id, i);
            }
        }
    }

    public static void renderIcon(GuiGraphics graphics, SpellData spell, int x, int y) {
        renderIcon(graphics, spell.school, getColumn(spell), x, y, ICON_SIZE);
    }

    public static void renderIconScaled(GuiGraphics graphics, SpellData spell, int x, int y, int size) {
        renderIcon(graphics, spell.school, getColumn(spell), x, y, size);
    }

    private static void renderIcon(GuiGraphics graphics, String school, int col, int x, int y, int size) {
        int row = SCHOOL_ORDER.indexOf(school);
        if (row < 0 || col < 0) return;

        int u = col * ICON_SIZE;
        int v = row * ICON_SIZE;

        RenderSystem.setShaderTexture(0, ATLAS);
        graphics.blit(ATLAS, x, y, size, size, u, v, ICON_SIZE, ICON_SIZE, COLS * ICON_SIZE, ROWS * ICON_SIZE);
    }

    private static int getColumn(SpellData spell) {
        return COLUMN_CACHE.getOrDefault(spell.id, -1);
    }
}
