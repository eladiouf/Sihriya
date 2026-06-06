package tong.sihriya.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;

import java.util.*;

public class SpellIconRenderer {
    public static final ResourceLocation ATLAS = new ResourceLocation(Sihriya.MODID, "textures/gui/spell_icons.png");
    public static final int ICON_SIZE = 32;
    public static final int COLS = 28;
    public static final int ROWS = 9;

    private static final List<String> SCHOOL_ORDER = Arrays.asList(
        "fire", "water", "wind", "earth",
        "lightning", "ice", "lava", "necromancy", "lumamancy"
    );

    private static final Map<String, Integer> COLUMN_CACHE = new HashMap<>();
    private static boolean initialized = false;

    public static void init() {
        SpellIconRenderer.ensureLoaded();
    }

    private static void ensureLoaded() {
        if (initialized && !COLUMN_CACHE.isEmpty()) return;
        COLUMN_CACHE.clear();
        if (SpellRegistry.size() == 0) return;
        for (String school : SCHOOL_ORDER) {
            int i = 0;
            for (SpellData spell : SpellRegistry.getBySchool(school)) {
                COLUMN_CACHE.put(spell.id, i++);
            }
        }
        initialized = true;
    }

    public static void renderIcon(GuiGraphics graphics, SpellData spell, int x, int y) {
        ensureLoaded();
        renderIcon(graphics, spell.school, getColumn(spell), x, y, ICON_SIZE);
    }

    public static void renderIconScaled(GuiGraphics graphics, SpellData spell, int x, int y, int size) {
        ensureLoaded();
        renderIcon(graphics, spell.school, getColumn(spell), x, y, size);
    }

    private static void renderIcon(GuiGraphics graphics, String school, int col, int x, int y, int size) {
        int row = SCHOOL_ORDER.indexOf(school);
        if (row < 0 || col < 0) return;

        int u = col * ICON_SIZE;
        int v = row * ICON_SIZE;
        graphics.blit(ATLAS, x, y, size, size, u, v, ICON_SIZE, ICON_SIZE, COLS * ICON_SIZE, ROWS * ICON_SIZE);
    }

    private static int getColumn(SpellData spell) {
        return COLUMN_CACHE.getOrDefault(spell.id, -1);
    }
}
