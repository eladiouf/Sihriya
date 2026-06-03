package tong.sihriya.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tong.sihriya.client.ClientSchoolData;
import tong.sihriya.client.KeyBindings;
import tong.sihriya.client.ClientManaData;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.network.SchoolCastPacket;

@OnlyIn(Dist.CLIENT)
public class SpellWheelScreen extends Screen {

    private static final int WHEEL_RADIUS = 80;
    private static final int ICON_SIZE = 32;
    private static final int DEAD_ZONE = 30;

    private int highlightedIndex = -1;
    private int cx, cy;
    private float alpha = 0f;

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
        if (ClientUiOptions.reducedMotion) {
            alpha = 1f;
        } else if (alpha < 1f) {
            alpha = Math.min(1f, alpha + 0.1f);
        }

        if (!KeyBindings.SPELL_WHEEL.isDown()) {
            if (highlightedIndex >= 0 && canCastHighlightedSchool()) {
                String school = getHighlightedSchool();
                ClientSchoolData.noteCastRequest(school);
                NetworkHandler.CHANNEL.sendToServer(new SchoolCastPacket(school));
            } else if (highlightedIndex >= 0) {
                String school = getHighlightedSchool();
                ClientSchoolData.getClientCastBlockReason(school)
                    .ifPresent(reason -> SihriyaNotifications.castBlocked(school, reason));
            } else {
                SihriyaUiSounds.close();
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

        if (dist > DEAD_ZONE) {
            float sector = 360f / SihriyaUiData.SCHOOL_ORDER.size();
            highlightedIndex = (int) ((angle + sector / 2) % 360 / sector);
            highlightedIndex = (9 - highlightedIndex + 2) % SihriyaUiData.SCHOOL_ORDER.size();
        } else {
            highlightedIndex = -1;
        }

        // Dark overlay
        g.fill(0, 0, width, height, 0x66000000);

        // Draw wheel rings
        drawWheelRings(g);

        // Draw school icons
        PoseStack pose = g.pose();
        for (int i = 0; i < SihriyaUiData.SCHOOL_ORDER.size(); i++) {
            String school = SihriyaUiData.SCHOOL_ORDER.get(i);
            float deg = i * 360f / SihriyaUiData.SCHOOL_ORDER.size() - 90f;
            float rad = (float) Math.toRadians(deg);
            int ix = cx + (int)(Math.cos(rad) * WHEEL_RADIUS) - ICON_SIZE / 2;
            int iy = cy + (int)(Math.sin(rad) * WHEEL_RADIUS) - ICON_SIZE / 2;

            boolean hl = (i == highlightedIndex);
            boolean unlocked = ClientSchoolData.isUnlocked(school);
            var displaySpell = ClientSchoolData.getBestCastableSpell(school)
                .or(() -> ClientSchoolData.getFirstSchoolSpell(school));
            float iconAlpha = unlocked ? alpha : 0.28f;

            if (displaySpell.isPresent()) {
                if (hl) {
                    drawGlowAt(g, ix + ICON_SIZE/2, iy + ICON_SIZE/2, ICON_SIZE + 10,
                        unlocked ? SihriyaUiData.schoolColor(school) : 0xFF777777);
                }

                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1, 1, 1, Math.min(1f, iconAlpha * (hl ? 1.2f : 1f)));
                SpellIconRenderer.renderIconScaled(g, displaySpell.get(), ix, iy, ICON_SIZE);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
            }

            if (!unlocked) {
                drawLock(g, ix + ICON_SIZE - 9, iy + ICON_SIZE - 9);
            }
        }

        // Center text
        pose.pushPose();
        pose.translate(cx, cy, 0);
        if (highlightedIndex >= 0) {
            renderSelectionText(g, pose);
        } else {
            String title = "Sihriya";
            int tw = font.width(title);
            font.drawInBatch(title, -tw/2f, -4, 0xFFFFFF, true, pose.last().pose(),
                g.bufferSource(), net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 0xF000F0);
            String hint = I18n.get("screen.sihriya.wheel.cancel");
            int hw = font.width(hint);
            font.drawInBatch(hint, -hw / 2f, WHEEL_RADIUS + ICON_SIZE + 24, 0xAAAAAA, true,
                pose.last().pose(), g.bufferSource(), net.minecraft.client.gui.Font.DisplayMode.NORMAL,
                0, 0xF000F0);
        }
        pose.popPose();
    }

    private void renderSelectionText(GuiGraphics g, PoseStack pose) {
        String school = getHighlightedSchool();
        boolean unlocked = ClientSchoolData.isUnlocked(school);
        int color = unlocked ? SihriyaUiData.schoolColor(school) : 0xFF888888;
        String name = SihriyaUiData.schoolName(school);
        drawCentered(g, pose, name, WHEEL_RADIUS + ICON_SIZE + 14, color);

        if (!unlocked) {
            drawCentered(g, pose, getRequirementText(school), WHEEL_RADIUS + ICON_SIZE + 28, 0xCCCCCC);
            drawCentered(g, pose, I18n.get("screen.sihriya.wheel.locked"), WHEEL_RADIUS + ICON_SIZE + 42, 0xFF7777);
            return;
        }

        var bestSpell = ClientSchoolData.getBestCastableSpell(school);
        if (bestSpell.isEmpty()) {
            drawCentered(g, pose, I18n.get("screen.sihriya.wheel.no_spell"), WHEEL_RADIUS + ICON_SIZE + 28, 0xFFAA66);
            return;
        }

        SpellData spell = bestSpell.get();
        String spellName = SihriyaUiData.spellName(spell);
        String details = I18n.get("screen.sihriya.wheel.spell_details",
            spell.tier, spell.manaCost, spell.cooldown / 20.0f);
        boolean enoughMana = ClientManaData.mana >= spell.manaCost && !ClientManaData.locked;
        drawCentered(g, pose, spellName, WHEEL_RADIUS + ICON_SIZE + 28, 0xFFFFFF);
        drawCentered(g, pose, details, WHEEL_RADIUS + ICON_SIZE + 42, enoughMana ? 0xAAAAAA : 0xFF7777);
    }

    private void drawCentered(GuiGraphics g, PoseStack pose, String text, int y, int color) {
        int w = font.width(text);
        font.drawInBatch(text, -w / 2f, y, color, true, pose.last().pose(), g.bufferSource(),
            net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 0xF000F0);
    }

    private void drawLock(GuiGraphics g, int x, int y) {
        g.fill(x + 2, y, x + 8, y + 2, 0xCCBBBBBB);
        g.fill(x, y + 3, x + 10, y + 10, 0xCC222222);
        g.fill(x + 3, y + 5, x + 7, y + 8, 0xCCBBBBBB);
    }

    private boolean canCastHighlightedSchool() {
        if (highlightedIndex < 0) return false;
        String school = getHighlightedSchool();
        return ClientSchoolData.isUnlocked(school) && ClientSchoolData.getBestCastableSpell(school).isPresent();
    }

    private String getHighlightedSchool() {
        return SihriyaUiData.SCHOOL_ORDER.get(highlightedIndex);
    }

    private String getRequirementText(String school) {
        return SihriyaUiData.unlockRequirementText(school, I18n.get("screen.sihriya.wheel.locked"));
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
