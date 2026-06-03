package tong.sihriya.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tong.sihriya.client.ClientSchoolData;
import tong.sihriya.client.KeyBindings;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.network.SchoolCastPacket;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SpellWheelScreen extends Screen {

    private static final int WHEEL_RADIUS = 80;
    private static final int ICON_SIZE = 32;

    private static final List<String> SCHOOLS = List.of(
        "fire", "water", "wind", "earth",
        "lightning", "ice", "lava", "necromancy", "lumamancy"
    );

    private static final int[] COLORS = {
        0xFFFF4500, 0xFF3399FF, 0xFF90EE90, 0xFF8B4513,
        0xFFFFD700, 0xFFADD8E6, 0xFFFF2200, 0xFF8C14DC, 0xFFFFDC50
    };

    private static final String[] NAMES = {
        "Feu", "Eau", "Vent", "Terre",
        "Foudre", "Glace", "Lave", "Necromancie", "Lumiere"
    };

    private int highlightedIndex = -1;
    private int cx, cy;
    private float alpha = 0f;
    private boolean castOnClose = true;

    public SpellWheelScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        cx = width / 2;
        cy = height / 2 - 20;
    }

    @Override
    public void tick() {
        if (alpha < 1f) alpha = Math.min(1f, alpha + 0.1f);

        if (!KeyBindings.SPELL_WHEEL.isDown()) {
            if (castOnClose && highlightedIndex >= 0) {
                NetworkHandler.CHANNEL.sendToServer(new SchoolCastPacket(SCHOOLS.get(highlightedIndex)));
            }
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx)) + 90f;
        if (angle < 0) angle += 360f;

        if (dist > 30) {
            float sector = 360f / SCHOOLS.size();
            highlightedIndex = (int) ((angle + sector / 2) % 360 / sector);
            highlightedIndex = (9 - highlightedIndex + 2) % SCHOOLS.size();
        } else {
            highlightedIndex = -1;
        }

        // Dark overlay
        g.fill(0, 0, width, height, 0x66000000);

        // Draw wheel rings
        drawWheelRings(g);

        // Draw school icons
        PoseStack pose = g.pose();
        for (int i = 0; i < SCHOOLS.size(); i++) {
            float deg = i * 360f / SCHOOLS.size() - 90f;
            float rad = (float) Math.toRadians(deg);
            int ix = cx + (int)(Math.cos(rad) * WHEEL_RADIUS) - ICON_SIZE / 2;
            int iy = cy + (int)(Math.sin(rad) * WHEEL_RADIUS) - ICON_SIZE / 2;

            boolean hl = (i == highlightedIndex);
            boolean unlocked = ClientSchoolData.isUnlocked(SCHOOLS.get(i));
            float iconAlpha = unlocked ? alpha : 0.3f;

            var spells = SpellRegistry.getBySchool(SCHOOLS.get(i));
            if (!spells.isEmpty()) {
                if (hl) {
                    drawGlowAt(g, ix + ICON_SIZE/2, iy + ICON_SIZE/2, ICON_SIZE + 10, COLORS[i]);
                }

                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1, 1, 1, iconAlpha * (hl ? 1.2f : 1f));
                SpellIconRenderer.renderIconScaled(g, spells.get(0), ix, iy, ICON_SIZE);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
            }
        }

        // Center text
        pose.pushPose();
        pose.translate(cx, cy, 0);
        if (highlightedIndex >= 0) {
            String name = NAMES[highlightedIndex];
            int w = font.width(name);
            font.drawInBatch(name, -w/2f, WHEEL_RADIUS + ICON_SIZE + 16,
                COLORS[highlightedIndex], true, pose.last().pose(), g.bufferSource(),
                net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 0xF000F0);
            String hint = "Relache pour lancer";
            int hw = font.width(hint);
            font.drawInBatch(hint, -hw/2f, WHEEL_RADIUS + ICON_SIZE + 30,
                0xAAAAAA, true, pose.last().pose(), g.bufferSource(),
                net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 0xF000F0);
        } else {
            String title = "Sihriya";
            int tw = font.width(title);
            font.drawInBatch(title, -tw/2f, -4, 0xFFFFFF, true, pose.last().pose(),
                g.bufferSource(), net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
        pose.popPose();
    }

    private void drawWheelRings(GuiGraphics g) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int j = 0; j < 5; j++) {
            float r = WHEEL_RADIUS + ICON_SIZE + 10 + j * 16;
            int a = (int)(10 * (1 - j / 5f) * alpha);
            if (a <= 0) continue;
            drawCircle(g, cx, cy, r, 64, a, 255, 255, 255);
        }
        RenderSystem.disableBlend();
    }

    private void drawGlowAt(GuiGraphics g, int x, int y, int r, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int red = (color >> 16) & 0xFF;
        int grn = (color >> 8) & 0xFF;
        int blu = color & 0xFF;
        for (int i = r / 2; i < r + 6; i += 2) {
            float t = (i - r / 2f) / (r + 6 - r / 2f);
            int a = (int)(100 * alpha * (1 - t) * (1 - t));
            if (a <= 0) continue;
            drawCircle(g, x, y, i, 32, a, red, grn, blu);
        }
        RenderSystem.disableBlend();
    }

    private void drawCircle(GuiGraphics g, int x, int y, float r, int segs, int a, int red, int grn, int blu) {
        Matrix4f mat = g.pose().last().pose();
        float af = a / 255f;
        float rf = red / 255f, gf = grn / 255f, bf = blu / 255f;
        RenderSystem.setShader(() -> net.minecraft.client.renderer.GameRenderer.getPositionColorShader());
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segs; i++) {
            float ang = (float)(i * 2 * Math.PI / segs);
            float cs = (float)Math.cos(ang), sn = (float)Math.sin(ang);
            bb.vertex(mat, x + cs * (r - 1), y + sn * (r - 1), 0).color(rf, gf, bf, af).endVertex();
            bb.vertex(mat, x + cs * (r + 1), y + sn * (r + 1), 0).color(rf, gf, bf, 0).endVertex();
        }
        Tesselator.getInstance().end();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}