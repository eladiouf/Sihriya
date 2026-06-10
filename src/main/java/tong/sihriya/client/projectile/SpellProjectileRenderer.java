package tong.sihriya.client.projectile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import tong.sihriya.client.vfx.render.SihriyaRenderUtils;
import tong.sihriya.projectile.SpellProjectile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile> {

    private static final ResourceLocation TEX_FIRE      = rl("minecraft", "block/fire_0");
    private static final ResourceLocation TEX_FIRE_CORE = rl("minecraft", "block/shroomlight");
    private static final ResourceLocation TEX_SOUL_FIRE = rl("minecraft", "block/soul_fire_0");
    private static final ResourceLocation TEX_WATER     = rl("minecraft", "block/water_still");
    private static final ResourceLocation TEX_ICE       = rl("minecraft", "block/packed_ice");
    private static final ResourceLocation TEX_MAGMA     = rl("minecraft", "block/magma");
    private static final ResourceLocation TEX_DIRT      = rl("minecraft", "block/dirt");
    private static final ResourceLocation TEX_STONE     = rl("minecraft", "block/stone");
    private static final ResourceLocation TEX_GLASS     = rl("minecraft", "block/glass");
    private static final ResourceLocation TEX_SEA_LANT  = rl("minecraft", "block/sea_lantern");
    private static final ResourceLocation TEX_SOUL_SAND = rl("minecraft", "block/soul_sand");
    private static final ResourceLocation TEX_GLOWSTONE = rl("minecraft", "block/glowstone");
    private static final ResourceLocation TEX_BEACON    = rl("minecraft", "block/beacon");

    private static final Map<String, String> SPELL_VARIANTS = buildVariantMap();

    private static Map<String, String> buildVariantMap() {
        Map<String, String> m = new HashMap<>();
        // fire
        m.put("spark",              "bolt");
        m.put("fireball",           "orb");
        m.put("blazing_sun",        "sun");
        m.put("comet",              "meteor");
        m.put("fire_piston_strike", "bolt");
        m.put("brands_mark",        "ember");
        m.put("fire_lance",         "lance");
        m.put("fire_wheel",         "disc");
        m.put("piercing_meteor",    "meteor");
        // water
        m.put("water_jet",  "stream");
        m.put("sharp_drop", "droplet");
        m.put("holy_water", "globe");
        m.put("water_lace", "stream");
        // wind
        m.put("gust",          "puff");
        m.put("whistle",       "blade");
        m.put("air_blade",     "blade");
        m.put("air_scissors",  "blade");
        m.put("silent_gust",   "puff");
        m.put("impaling_wind", "lance");
        // earth
        m.put("rock_strike",    "chunk");
        m.put("rock_spike",     "spike");
        m.put("clay_projectile","orb");
        m.put("blinding_dust",  "dust");
        m.put("stone_vise",     "chunk");
        m.put("shield_strike",  "disc");
        m.put("stone_statue",   "chunk");
        m.put("drill_through",  "lance");
        m.put("earth_column",   "column");
        // lightning
        m.put("electric_arc",    "bolt");
        m.put("chain_lightning", "chain");
        m.put("paralysis",       "orb");
        m.put("guided_lightning","orb");
        m.put("bouncing_spark",  "spark");
        m.put("lightning_link",  "bolt");
        m.put("underground_arc", "bolt");
        m.put("rainbow_arc",     "rainbow");
        // ice
        m.put("ice_arrow",      "arrow");
        m.put("iceberg_lance",  "lance");
        m.put("frost_link",     "stream");
        m.put("diamond_prison", "crystal");
        m.put("shiver",         "arrow");
        // lava
        m.put("lava_jet",       "stream");
        m.put("magma_bomb",     "orb");
        m.put("volcanic_tears", "droplet");
        m.put("magma_grenade",  "chunk");
        m.put("igneous_rock",   "chunk");
        // necromancy
        m.put("drain_vital",       "wisp");
        m.put("death_touch",       "orb");
        m.put("lethal_curse",      "wisp");
        m.put("soul_rend",         "bolt");
        m.put("soul_chains",       "shard");
        m.put("soul_corruption",   "wisp");
        m.put("decomposition",     "shard");
        m.put("mark_of_sacrifice", "orb");
        m.put("broken_bones",      "shard");
        m.put("empty_gaze",        "wisp");
        m.put("putrid_hand",       "orb");
        m.put("cold_fingers",      "shard");
        // lumamancy
        m.put("light_shard",   "shard");
        m.put("holy_strike",   "orb");
        m.put("blinding_ray",  "beam");
        m.put("avenging_light","meteor");
        return Collections.unmodifiableMap(m);
    }

    public SpellProjectileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(SpellProjectile entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        boolean charging = entity.isCharging();
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() < 1e-6 && !charging) return;

        String school   = entity.getSchoolId();
        String spellRaw = entity.getSpellId();
        String spellKey = spellRaw.contains(".") ? spellRaw.substring(spellRaw.indexOf('.') + 1) : spellRaw;
        String variant  = SPELL_VARIANTS.getOrDefault(spellKey, "default");

        pose.pushPose();
        pose.scale(0.25f, 0.25f, 0.25f);

        // Scale charge (1.0x → 8.0x) toujours appliqué, même après lancement
        float chargeScale = entity.getChargeScale();
        pose.scale(chargeScale, chargeScale, chargeScale);

        if (charging) {
            pose.mulPose(Axis.YP.rotationDegrees(yaw));
        } else {
            Vec3 dir = motion.normalize();
            pose.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(Math.atan2(dir.x, dir.z))));
            pose.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(Math.asin(-dir.y))));
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f mat = new Matrix4f();

        switch (school) {
            case "fire"       -> renderFire(buffer, pose, light, mat, variant, entity);
            case "lava"       -> renderLava(buffer, pose, light, mat, variant);
            case "water"      -> renderWater(buffer, pose, light, mat, variant);
            case "ice"        -> renderIce(buffer, pose, light, mat, variant);
            case "lightning"  -> renderLightning(buffer, pose, light, mat, variant);
            case "wind"       -> renderWind(buffer, pose, light, mat, variant);
            case "earth"      -> renderEarth(buffer, pose, light, mat, variant);
            case "necromancy" -> renderNecromancy(buffer, pose, light, mat, variant);
            case "lumamancy"  -> renderLumamancy(buffer, pose, light, mat, variant);
            default           -> renderFire(buffer, pose, light, mat, "default", entity);
        }

        RenderSystem.disableBlend();
        pose.popPose();
    }

    // ── FIRE ──────────────────────────────────────────────────────────────────
    private static void renderFire(MultiBufferSource buf, PoseStack pose, int light,
                                    Matrix4f mat, String variant, SpellProjectile entity) {
        VertexConsumer vc = buf.getBuffer(RenderType.cutout());
        switch (variant) {
            case "bolt" -> { // spark, fire_piston_strike — thin fast streak
                pose.pushPose(); pose.scale(0.45f, 0.45f, 1.2f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.9f, 0.2f, 1f, TEX_FIRE, 3.5f, mat, false, false, true);
                pose.popPose();
            }
            case "lance" -> { // fire_lance — narrow spear
                pose.pushPose(); pose.scale(0.28f, 0.28f, 1.4f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.5f, 0f, 1f, TEX_FIRE, 5.0f, mat, false, false, true);
                pose.popPose();
            }
            case "orb" -> { // fireball — round globe
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.5f, 0.1f, 1f, TEX_FIRE, 1.2f, mat, true, true, true);
                pose.pushPose(); pose.scale(0.7f, 0.7f, 0.7f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.9f, 0.5f, 0.7f, TEX_FIRE_CORE, 1.2f, mat, true, true, true);
                pose.popPose();
            }
            case "ember" -> { // brands_mark — small dark ember
                pose.pushPose(); pose.scale(0.6f, 0.6f, 0.6f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 0.8f, 0.3f, 0f, 0.9f, TEX_FIRE, 1.0f, mat, false, true, true);
                pose.popPose();
            }
            case "disc" -> { // fire_wheel — wide flat disc
                pose.pushPose(); pose.scale(1.8f, 1.8f, 0.2f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.4f, 0f, 0.95f, TEX_FIRE, 1.0f, mat, false, true, true);
                pose.popPose();
            }
            case "meteor" -> { // comet, piercing_meteor — large blazing rock
                pose.pushPose(); pose.scale(1.3f, 1.3f, 1f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.4f, 0f, 1f, TEX_FIRE, 2.5f, mat, false, true, true);
                pose.popPose();
                pose.pushPose(); pose.scale(0.9f, 0.9f, 0.9f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.8f, 0.2f, 0.7f, TEX_FIRE_CORE, 3.0f, mat, false, true, true);
                pose.popPose();
            }
            case "sun" -> { // blazing_sun — enormous fiery sun cubique qui tourne
                float spin = entity.tickCount * 2.0f;
                // Layer 1: outer fire glow
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(spin));
                pose.scale(25.0f, 25.0f, 25.0f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.6f, 0.05f, 0.9f, TEX_FIRE, 1.0f, mat, true, true, true);
                pose.popPose();
                // Layer 2: inner hot core
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(spin));
                pose.scale(18.0f, 18.0f, 18.0f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.85f, 0.2f, 0.85f, TEX_FIRE_CORE, 1.0f, mat, true, true, true);
                pose.popPose();
                // Layer 3: bright center
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(spin));
                pose.scale(10.0f, 10.0f, 10.0f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 0.7f, 1f, TEX_BEACON, 1.0f, mat, true, true, true);
                pose.popPose();
            }
            default -> {
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 1f, 1f, TEX_FIRE, 2f, mat, false, false, false);
                pose.pushPose(); pose.scale(0.8f, 0.8f, 0.8f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.96f, 0.6f, 0.5f, TEX_FIRE_CORE, 2.5f, mat, false, true, true);
                pose.popPose();
            }
        }
    }

    // ── LAVA ──────────────────────────────────────────────────────────────────
    private static void renderLava(MultiBufferSource buf, PoseStack pose, int light,
                                    Matrix4f mat, String variant) {
        VertexConsumer vc = buf.getBuffer(RenderType.cutout());
        switch (variant) {
            case "stream" -> { // lava_jet — thin molten stream
                pose.pushPose(); pose.scale(0.4f, 0.4f, 1.2f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.5f, 0.1f, 1f, TEX_MAGMA, 3.5f, mat, false, false, true);
                pose.popPose();
            }
            case "orb" -> { // magma_bomb — heavy glowing bomb
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.4f, 0.1f, 1f, TEX_MAGMA, 1.5f, mat, false, true, true);
                pose.pushPose(); pose.scale(0.7f, 0.7f, 0.7f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.6f, 0f, 0.8f, TEX_FIRE_CORE, 1.5f, mat, false, true, true);
                pose.popPose();
            }
            case "droplet" -> { // volcanic_tears — small falling drop
                pose.pushPose(); pose.scale(0.65f, 0.65f, 0.65f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.4f, 0f, 0.95f, TEX_MAGMA, 0.9f, mat, false, true, true);
                pose.popPose();
            }
            case "chunk" -> { // magma_grenade, igneous_rock — solid rock lump
                pose.pushPose(); pose.scale(1.1f, 1.1f, 1.1f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 0.9f, 0.35f, 0.05f, 1f, TEX_MAGMA, 1.5f, mat, false, true, true);
                pose.popPose();
            }
            default -> {
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.5f, 0.1f, 1f, TEX_MAGMA, 2f, mat, false, false, false);
                pose.pushPose(); pose.scale(0.7f, 0.7f, 0.7f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.3f, 0f, 0.6f, TEX_FIRE_CORE, 2.5f, mat, false, true, true);
                pose.popPose();
            }
        }
    }

    // ── WATER ─────────────────────────────────────────────────────────────────
    private static void renderWater(MultiBufferSource buf, PoseStack pose, int light,
                                     Matrix4f mat, String variant) {
        VertexConsumer vc = buf.getBuffer(RenderType.translucent());
        switch (variant) {
            case "stream" -> { // water_jet — narrow pressurized stream
                pose.pushPose(); pose.scale(0.4f, 0.4f, 1.2f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.3f, 0.6f, 1f, 0.9f, TEX_WATER, 3.0f, mat, false, false, true);
                pose.popPose();
            }
            case "droplet" -> { // sharp_drop — small fast droplet
                pose.pushPose(); pose.scale(0.6f, 0.6f, 0.6f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.3f, 0.6f, 1f, 0.95f, TEX_WATER, 0.9f, mat, false, true, true);
                pose.popPose();
            }
            case "globe" -> { // holy_water, water_lace — glowing water orb
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.3f, 0.6f, 1f, 0.85f, TEX_WATER, 1.1f, mat, true, true, true);
                pose.pushPose(); pose.scale(0.7f, 0.7f, 0.7f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.6f, 0.85f, 1f, 0.55f, TEX_WATER, 1.1f, mat, true, true, true);
                pose.popPose();
            }
            default -> {
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.3f, 0.6f, 1f, 0.9f, TEX_WATER, 2f, mat, false, true, true);
                pose.pushPose(); pose.scale(0.65f, 0.65f, 0.65f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.5f, 0.8f, 1f, 0.6f, TEX_WATER, 2.5f, mat, false, true, true);
                pose.popPose();
            }
        }
    }

    // ── ICE ───────────────────────────────────────────────────────────────────
    private static void renderIce(MultiBufferSource buf, PoseStack pose, int light,
                                   Matrix4f mat, String variant) {
        VertexConsumer vc = buf.getBuffer(RenderType.translucent());
        switch (variant) {
            case "arrow" -> { // ice_arrow, shiver — thin ice bolt
                pose.pushPose(); pose.scale(0.35f, 0.35f, 1.4f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.75f, 0.92f, 1f, 0.9f, TEX_ICE, 4.0f, mat, false, false, true);
                pose.popPose();
            }
            case "lance" -> { // iceberg_lance — long ice spear
                pose.pushPose(); pose.scale(0.22f, 0.22f, 1.8f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.85f, 0.95f, 1f, 0.95f, TEX_ICE, 6.0f, mat, false, false, true);
                pose.popPose();
            }
            case "stream" -> { // frost_link — icy connecting beam
                pose.pushPose(); pose.scale(0.45f, 0.45f, 1f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.6f, 0.85f, 1f, 0.75f, TEX_WATER, 3.0f, mat, false, false, true);
                pose.popPose();
            }
            case "crystal" -> { // diamond_prison — chunky crystal cluster
                pose.pushPose(); pose.scale(1.1f, 1.1f, 1.1f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.7f, 0.9f, 1f, 0.8f, TEX_ICE, 1.5f, mat, false, true, true);
                pose.popPose();
                pose.pushPose(); pose.scale(0.75f, 0.75f, 0.75f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.9f, 0.97f, 1f, 0.45f, TEX_GLASS, 1.5f, mat, false, true, true);
                pose.popPose();
            }
            default -> {
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.7f, 0.9f, 1f, 0.85f, TEX_ICE, 2f, mat, false, true, true);
                pose.pushPose(); pose.scale(0.6f, 0.6f, 0.6f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.85f, 0.95f, 1f, 0.5f, TEX_ICE, 2.4f, mat, false, true, true);
                pose.popPose();
            }
        }
    }

    // ── LIGHTNING ─────────────────────────────────────────────────────────────
    private static void renderLightning(MultiBufferSource buf, PoseStack pose, int light,
                                         Matrix4f mat, String variant) {
        VertexConsumer vc = buf.getBuffer(RenderType.translucent());
        switch (variant) {
            case "orb" -> { // guided_lightning — floating electric orb
                for (int i = 1; i <= 3; i++) {
                    float s = (float) i / 3;
                    Matrix4f scaled = new Matrix4f(mat).scale(s, s, s);
                    float a = (1f - (i - 1) * 0.3f) * 0.9f;
                    SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 0.5f, a, TEX_SEA_LANT, 1.2f, scaled, true, true, true);
                }
            }
            case "chain" -> { // chain_lightning — branched electric arcs
                // Main bolt
                for (int i = 1; i <= 4; i++) {
                    Matrix4f scaled = new Matrix4f(mat).scale((float) i / 4, (float) i / 4, (float) i / 4);
                    float a = (4f / i) / 4f;
                    float d = 2f * (4f / i);
                    SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 0.6f, a, TEX_SEA_LANT, d, scaled, false, true, true);
                }
                // Branch offshoots at 45° offset
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(25));
                pose.mulPose(Axis.XP.rotationDegrees(15));
                for (int i = 1; i <= 3; i++) {
                    Matrix4f scaled = new Matrix4f(mat).scale((float) i / 4, (float) i / 4, (float) i / 4);
                    float a = ((4f / i) / 4f) * 0.5f;
                    float d = 1.2f * (4f / i);
                    SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.8f, 0.3f, a, TEX_SEA_LANT, d, scaled, false, true, true);
                }
                pose.popPose();
                // Branch offshoots at -45° offset
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(-25));
                pose.mulPose(Axis.XP.rotationDegrees(-15));
                for (int i = 1; i <= 3; i++) {
                    Matrix4f scaled = new Matrix4f(mat).scale((float) i / 4, (float) i / 4, (float) i / 4);
                    float a = ((4f / i) / 4f) * 0.5f;
                    float d = 1.2f * (4f / i);
                    SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 0.8f, 0.3f, a, TEX_SEA_LANT, d, scaled, false, true, true);
                }
                pose.popPose();
            }
            case "spark" -> { // bouncing_spark — tiny bright spark
                pose.pushPose(); pose.scale(0.4f, 0.4f, 0.4f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 0.3f, 1f, TEX_SEA_LANT, 1.0f, mat, false, true, true);
                pose.popPose();
            }
            case "rainbow" -> { // rainbow_arc — multi-color concentric rings
                float[][] cols = {{1f,0.2f,0.2f},{1f,0.7f,0.1f},{0.2f,1f,0.9f},{0.8f,0.2f,1f}};
                for (int i = 1; i <= 4; i++) {
                    Matrix4f scaled = new Matrix4f(mat).scale((float) i / 4, (float) i / 4, (float) i / 4);
                    float a = (4f / i) / 4f;
                    float[] c = cols[i - 1];
                    SihriyaRenderUtils.drawCube(vc, pose, 255, c[0], c[1], c[2], a, TEX_SEA_LANT, 2f * (4f / i), scaled, false, true, true);
                }
            }
            default -> { // bolt — original 4-layer electric beam
                for (int i = 1; i <= 4; i++) {
                    Matrix4f scaled = new Matrix4f(mat).scale((float) i / 4, (float) i / 4, (float) i / 4);
                    float a = (4f / i) / 4f;
                    float d = 2f * (4f / i);
                    SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 0.6f, a, TEX_SEA_LANT, d, scaled, false, true, true);
                }
            }
        }
    }

    // ── WIND ──────────────────────────────────────────────────────────────────
    private static void renderWind(MultiBufferSource buf, PoseStack pose, int light,
                                    Matrix4f mat, String variant) {
        VertexConsumer vc = buf.getBuffer(RenderType.translucent());
        switch (variant) {
            case "puff" -> { // gust, silent_gust — wide short burst
                pose.pushPose(); pose.scale(1.4f, 1.4f, 0.4f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.95f, 1f, 1f, 0.3f, TEX_GLASS, 1.0f, mat, false, true, true);
                pose.popPose();
            }
            case "blade" -> { // air_blade, air_scissors, whistle — thin vertical slash
                pose.pushPose(); pose.scale(0.15f, 1.6f, 1f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.9f, 0.97f, 1f, 0.5f, TEX_GLASS, 3.0f, mat, false, false, true);
                pose.popPose();
            }
            case "lance" -> { // impaling_wind — concentrated wind needle
                pose.pushPose(); pose.scale(0.3f, 0.3f, 1.2f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.9f, 0.97f, 1f, 0.55f, TEX_GLASS, 4.5f, mat, false, false, true);
                pose.popPose();
            }
            default -> {
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.9f, 0.97f, 1f, 0.45f, TEX_GLASS, 2f, mat, false, true, true);
                pose.pushPose(); pose.scale(0.8f, 0.8f, 0.8f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 1f, 1f, 1f, 0.25f, TEX_GLASS, 2f, mat, false, true, true);
                pose.popPose();
            }
        }
    }

    // ── EARTH ─────────────────────────────────────────────────────────────────
    private static void renderEarth(MultiBufferSource buf, PoseStack pose, int light,
                                     Matrix4f mat, String variant) {
        VertexConsumer vc = buf.getBuffer(RenderType.cutout());
        switch (variant) {
            case "chunk" -> { // rock_strike, stone_vise, stone_statue
                pose.pushPose(); pose.scale(1.1f, 1.1f, 1.1f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.6f, 0.4f, 0.2f, 1f, TEX_DIRT, TEX_STONE, 1.5f, mat, false, true, true);
                pose.popPose();
            }
            case "spike" -> { // rock_spike — long stone spike
                pose.pushPose(); pose.scale(0.35f, 0.35f, 1.2f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.55f, 0.55f, 0.55f, 1f, TEX_STONE, 4.5f, mat, false, false, true);
                pose.popPose();
            }
            case "orb" -> { // clay_projectile — compact clay ball
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.7f, 0.5f, 0.3f, 1f, TEX_DIRT, 1.2f, mat, false, true, true);
            }
            case "dust" -> { // blinding_dust — wide transparent cloud
                pose.pushPose(); pose.scale(1.5f, 1.5f, 0.5f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.7f, 0.6f, 0.4f, 0.5f, TEX_DIRT, 1.0f, mat, false, true, true);
                pose.popPose();
            }
            case "disc" -> { // shield_strike — flat thrown stone disc
                pose.pushPose(); pose.scale(1.8f, 1.8f, 0.2f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.55f, 0.55f, 0.55f, 1f, TEX_STONE, 1.0f, mat, false, true, true);
                pose.popPose();
            }
            case "lance" -> { // drill_through — narrow drilling spike
                pose.pushPose(); pose.scale(0.28f, 0.28f, 1.5f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.5f, 0.5f, 0.5f, 1f, TEX_STONE, 5.5f, mat, false, false, true);
                pose.popPose();
            }
            case "column" -> { // earth_column — tall wide pillar
                pose.pushPose(); pose.scale(1.2f, 1.2f, 2f);
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.6f, 0.4f, 0.2f, 1f, TEX_DIRT, TEX_STONE, 2.5f, mat, false, true, true);
                pose.popPose();
            }
            default -> {
                SihriyaRenderUtils.drawCube(vc, pose, light, 0.6f, 0.4f, 0.2f, 1f, TEX_DIRT, TEX_STONE, 2f, mat, false, true, true);
            }
        }
    }

    // ── NECROMANCY ────────────────────────────────────────────────────────────
    private static void renderNecromancy(MultiBufferSource buf, PoseStack pose, int light,
                                          Matrix4f mat, String variant) {
        VertexConsumer vc = buf.getBuffer(RenderType.cutout());
        switch (variant) {
            case "wisp" -> { // drain_vital, lethal_curse, soul_corruption, empty_gaze
                pose.pushPose(); pose.scale(0.8f, 0.8f, 0.8f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 0.3f, 0f, 0.6f, 0.85f, TEX_SOUL_FIRE, 0.9f, mat, true, true, true);
                pose.popPose();
                pose.pushPose(); pose.scale(0.55f, 0.55f, 0.55f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 0.5f, 0f, 0.8f, 0.5f, TEX_SOUL_FIRE, 0.9f, mat, true, true, true);
                pose.popPose();
            }
            case "bolt" -> { // soul_rend — dark bolt of soul energy
                pose.pushPose(); pose.scale(0.4f, 0.4f, 1.1f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 0.4f, 0f, 0.7f, 0.9f, TEX_SOUL_FIRE, 3.5f, mat, false, false, true);
                pose.popPose();
            }
            case "orb" -> { // death_touch, mark_of_sacrifice, putrid_hand
                SihriyaRenderUtils.drawCube(vc, pose, 255, 0.4f, 0.1f, 0.7f, 0.9f, TEX_SOUL_SAND, 1.0f, mat, false, true, true);
                pose.pushPose(); pose.scale(0.7f, 0.7f, 0.7f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 0.6f, 0f, 0.9f, 0.55f, TEX_SOUL_FIRE, 1.0f, mat, false, true, true);
                pose.popPose();
            }
            case "shard" -> { // broken_bones, cold_fingers, soul_chains, decomposition
                pose.pushPose(); pose.scale(0.22f, 0.22f, 1.3f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 0.55f, 0.55f, 0.65f, 0.9f, TEX_SOUL_FIRE, 3.0f, mat, false, false, true);
                pose.popPose();
            }
            default -> {
                SihriyaRenderUtils.drawCube(vc, pose, 255, 0.4f, 0.1f, 0.7f, 0.9f, TEX_SOUL_FIRE, 2f, mat, false, false, false);
                pose.pushPose(); pose.scale(0.75f, 0.75f, 0.75f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 0.6f, 0f, 0.9f, 0.5f, TEX_SOUL_SAND, 2.5f, mat, false, true, true);
                pose.popPose();
            }
        }
    }

    // ── LUMAMANCY ─────────────────────────────────────────────────────────────
    private static void renderLumamancy(MultiBufferSource buf, PoseStack pose, int light,
                                         Matrix4f mat, String variant) {
        VertexConsumer vc = buf.getBuffer(RenderType.translucent());
        switch (variant) {
            case "shard" -> { // light_shard — ultra-thin needle of pure light
                pose.pushPose(); pose.scale(0.2f, 0.2f, 1.5f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 0.9f, 1f, TEX_BEACON, 4.0f, mat, false, false, true);
                pose.popPose();
            }
            case "orb" -> { // holy_strike — radiant holy orb
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 0.9f, 0.9f, TEX_GLOWSTONE, 1.2f, mat, true, true, true);
                pose.pushPose(); pose.scale(0.7f, 0.7f, 0.7f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 1f, 0.65f, TEX_BEACON, 1.2f, mat, true, true, true);
                pose.popPose();
            }
            case "beam" -> { // blinding_ray — bright narrow beam with outer glow
                pose.pushPose(); pose.scale(0.4f, 0.4f, 1.3f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 0.8f, 0.95f, TEX_BEACON, 3.5f, mat, false, false, true);
                pose.popPose();
                pose.pushPose(); pose.scale(0.75f, 0.75f, 1f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 1f, 0.45f, TEX_GLOWSTONE, 3.0f, mat, false, false, true);
                pose.popPose();
            }
            case "meteor" -> { // avenging_light — large holy comet
                pose.pushPose(); pose.scale(1.4f, 1.4f, 1f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 0.8f, 0.9f, TEX_GLOWSTONE, 2.5f, mat, false, true, true);
                pose.popPose();
                pose.pushPose(); pose.scale(0.85f, 0.85f, 0.85f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 1f, 0.6f, TEX_BEACON, 3.0f, mat, false, true, true);
                pose.popPose();
            }
            default -> {
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 0.8f, 0.9f, TEX_GLOWSTONE, 2f, mat, false, true, true);
                pose.pushPose(); pose.scale(0.7f, 0.7f, 0.7f);
                SihriyaRenderUtils.drawCube(vc, pose, 255, 1f, 1f, 1f, 0.6f, TEX_BEACON, 2.5f, mat, false, true, true);
                pose.popPose();
            }
        }
    }

    private static ResourceLocation rl(String ns, String path) {
        return new ResourceLocation(ns, path);
    }

    @Override
    public ResourceLocation getTextureLocation(SpellProjectile entity) {
        return TEX_FIRE;
    }
}
