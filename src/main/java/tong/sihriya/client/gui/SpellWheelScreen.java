package tong.sihriya.client.gui;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import tong.sihriya.Sihriya;
import tong.sihriya.client.ClientSchoolData;
import tong.sihriya.data.SpellRegistry;

import java.util.*;

public class SpellWheelScreen extends Screen {
    private static final int WHEEL_RADIUS = 60;
    private static final int SLOT_RADIUS = 18;
    private static final int CENTER_X = 0;
    private static final int CENTER_Y = 0;
    private static final int SEGMENT_ANGLE = 360 / 8;

    private String selectedSpell = null;
    private List<String> spells = new ArrayList<>();

    public SpellWheelScreen() {
        super(Component.literal("Spell Wheel"));
    }

    @Override
    protected void init() {
        super.init();
        // Get learned spells for active school
        spells.clear();
        String active = ClientSchoolData.activeSchool;
        String prefix = active.isEmpty() ? "" : active + ".";
        for (String spellId : ClientSchoolData.learnedSpells) {
            if (spellId.startsWith(prefix) || prefix.isEmpty()) {
                spells.add(spellId);
            }
        }
        // If no specific school spells, show all learned
        if (spells.isEmpty()) {
            spells.addAll(ClientSchoolData.learnedSpells);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int cx = width / 2;
        int cy = height / 2;

        // Darken background
        graphics.fill(0, 0, width, height, 0x80000000);

        // Draw wheel background
        fillCircle(graphics, cx, cy, WHEEL_RADIUS + 10, 0xAA333333);

        // Draw school name at center
        String schoolName = !ClientSchoolData.activeSchool.isEmpty()
            ? ClientSchoolData.activeSchool.toUpperCase() : "ALL";
        graphics.drawCenteredString(font, schoolName, cx, cy - 4, 0xFFD700);
        int level = ClientSchoolData.schoolLevels.getOrDefault(ClientSchoolData.activeSchool, 0);
        graphics.drawCenteredString(font, "Lv." + level, cx, cy + 8, 0xAAAAAA);

        // Draw spells around the wheel
        int count = Math.min(spells.size(), 8);
        if (count == 0) return;

        double angleStep = 2 * Math.PI / count;
        double startAngle = -Math.PI / 2; // start from top

        for (int i = 0; i < count; i++) {
            double angle = startAngle + i * angleStep;
            int sx = cx + (int)(Math.cos(angle) * WHEEL_RADIUS);
            int sy = cy + (int)(Math.sin(angle) * WHEEL_RADIUS);

            // Check if mouse is hovering this slot
            double dist = Math.sqrt((mouseX - sx) * (mouseX - sx) + (mouseY - sy) * (mouseY - sy));
            boolean hovered = dist < SLOT_RADIUS;

            // Draw slot background
            int slotColor = hovered ? 0xAAFFD700 : 0xAA444444;
            fillCircle(graphics, sx, sy, SLOT_RADIUS, slotColor);

            if (hovered) {
                selectedSpell = spells.get(i);
            }

            // Draw spell name
            String spellName = spells.get(i);
            if (spellName.contains(".")) spellName = spellName.split("\\.")[1];
            graphics.drawCenteredString(font, spellName, sx, sy - 4, 0xFFFFFF);
        }
    }

    public String getSelectedSpell() { return selectedSpell; }

    private void fillCircle(GuiGraphics graphics, int x, int y, int radius, int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(x, y, 0).color(r, g, b, a).endVertex();
        for (int i = 0; i <= 32; i++) {
            double angle = 2 * Math.PI * i / 32;
            buffer.vertex(
                x + Math.cos(angle) * radius,
                y + Math.sin(angle) * radius,
                0
            ).color(r, g, b, a).endVertex();
        }
        tesselator.end();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Select on click
        if (selectedSpell != null) {
            // Will be cast on key release
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
