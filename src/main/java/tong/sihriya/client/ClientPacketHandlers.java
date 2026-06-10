package tong.sihriya.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import tong.sihriya.Sihriya;
import tong.sihriya.client.gui.SihriyaNotifications;
import tong.sihriya.client.gui.SihriyaUiSounds;
import tong.sihriya.client.vfx.VFXEffect;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.registry.SihriyaParticles;
import tong.sihriya.client.vfx.VFXEngine;
import tong.sihriya.client.vfx.render.ImpactHandler;
import tong.sihriya.client.vfx.render.ScreenShakeHandler;
import tong.sihriya.client.vfx.render.VFXFactory;
import tong.sihriya.config.SihriyaClientConfig;
import tong.sihriya.network.ClientPacketBridge;
import tong.sihriya.vfx.VFXDefinition;
import tong.sihriya.vfx.VFXRegistry;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientPacketHandlers {
    private ClientPacketHandlers() {}

    private static final ResourceLocation TEX_CIRCLE_RINGS  = rl("textures/vfx/kenney/circle_rings_a_streaks.png");
    private static final ResourceLocation TEX_CIRCLE_NOISE  = rl("textures/vfx/kenney/circle_rings_a_noise.png");
    private static final ResourceLocation TEX_CIRCLE_INNER  = rl("textures/vfx/kenney/circle_c_streaks.png");

    private static List<Vector2f> POINTS_RINGS;
    private static List<Vector2f> POINTS_NOISE;
    private static List<Vector2f> POINTS_INNER;
    private static boolean pointsLoaded = false;

    private static final java.util.Map<Integer, ActiveCircle> ACTIVE_CIRCLES = new java.util.HashMap<>();
    private static int nextCircleId = 0;

    private static class ActiveCircle {
        final ClientLevel level;
        final Vec3 pos;
        final String schoolId;
        final int totalTicks;
        final double maxRadius;
        int age;

        ActiveCircle(ClientLevel level, Vec3 pos, String schoolId, int totalTicks, double radius) {
            this.level = level;
            this.pos = pos;
            this.schoolId = schoolId;
            this.totalTicks = totalTicks;
            this.maxRadius = radius;
            this.age = 0;
        }
    }

    public static void register() {
        ClientPacketBridge.registerHandlers(
            ClientPacketHandlers::handleManaSync,
            ClientPacketHandlers::handleSchoolSync,
            ClientPacketHandlers::handleCastResult,
            ClientPacketHandlers::handleSpellParticles,
            ClientPacketHandlers::handleVFXTrigger
        );
    }

    public static void onClientTick() {
        if (ACTIVE_CIRCLES.isEmpty()) return;
        if (!pointsLoaded) {
            pointsLoaded = true;
            POINTS_RINGS = CircleTextureSampler.sample(TEX_CIRCLE_RINGS);
            POINTS_NOISE = CircleTextureSampler.sample(TEX_CIRCLE_NOISE);
            POINTS_INNER = CircleTextureSampler.sample(TEX_CIRCLE_INNER);
        }
        Iterator<Map.Entry<Integer, ActiveCircle>> it = ACTIVE_CIRCLES.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            ActiveCircle c = entry.getValue();
            c.age++;
            if (c.age > c.totalTicks) {
                it.remove();
                continue;
            }
            spawnCircleTick(c);
        }
    }

    private static void spawnCircleTick(ActiveCircle c) {
        var particle = "fire".equals(c.schoolId) || "lava".equals(c.schoolId)
            ? net.minecraft.core.particles.ParticleTypes.FLAME
            : SihriyaParticles.getForSchool(c.schoolId);
        double radius = c.maxRadius;
        double angle = c.age * 0.015;

        if (POINTS_RINGS != null) {
            int step = Math.max(1, POINTS_RINGS.size() / 120);
            for (int i = 0; i < POINTS_RINGS.size(); i += step) {
                Vector2f p = POINTS_RINGS.get(i);
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double rx = p.x() * radius;
                double rz = p.y() * radius;
                double wx = c.pos.x + rx * cos - rz * sin;
                double wz = c.pos.z + rx * sin + rz * cos;
                c.level.addParticle(particle, true, wx, c.pos.y + 0.2, wz, 0, 0.002, 0);
            }
        }

        if (POINTS_NOISE != null) {
            int step = Math.max(1, POINTS_NOISE.size() / 60);
            for (int i = 0; i < POINTS_NOISE.size(); i += step) {
                Vector2f p = POINTS_NOISE.get(i);
                double cos = Math.cos(-angle * 0.7);
                double sin = Math.sin(-angle * 0.7);
                double rx = p.x() * radius * 0.8;
                double rz = p.y() * radius * 0.8;
                double wx = c.pos.x + rx * cos - rz * sin;
                double wz = c.pos.z + rx * sin + rz * cos;
                c.level.addParticle(particle, true, wx, c.pos.y + 0.5, wz, 0, 0.003, 0);
            }
        }

        if (POINTS_INNER != null) {
            int step = Math.max(1, POINTS_INNER.size() / 40);
            for (int i = 0; i < POINTS_INNER.size(); i += step) {
                Vector2f p = POINTS_INNER.get(i);
                double cos = Math.cos(angle * 1.3);
                double sin = Math.sin(angle * 1.3);
                double rx = p.x() * radius * 0.4;
                double rz = p.y() * radius * 0.4;
                double wx = c.pos.x + rx * cos - rz * sin;
                double wz = c.pos.z + rx * sin + rz * cos;
                c.level.addParticle(particle, true, wx, c.pos.y + 0.8, wz, 0, 0.004, 0);
            }
        }

        // Rising sparkles
        int sparks = 6 + c.level.random.nextInt(6);
        for (int i = 0; i < sparks; i++) {
            double a = c.level.random.nextDouble() * Math.PI * 2;
            double r = c.level.random.nextDouble() * radius * 0.9;
            double x = c.pos.x + Math.cos(a) * r;
            double z = c.pos.z + Math.sin(a) * r;
            c.level.addParticle(particle, true,
                x, c.pos.y + 0.1 + c.level.random.nextDouble() * 1.5, z,
                0, 0.03 + c.level.random.nextDouble() * 0.04, 0);
        }
    }

    private static void handleManaSync(float mana, float maxMana, boolean locked, long lockRemaining) {
        ClientSchoolData.mana = mana;
        ClientSchoolData.maxMana = maxMana;
        ClientSchoolData.manaBlocked = locked;
        ClientSchoolData.manaBlockRemainingMs = lockRemaining;
    }

    private static void handleSchoolSync(String activeSchool, Map<String, Integer> schoolLevels,
                                          Set<String> unlockedSchools, Set<String> learnedSpells) {
        ClientSchoolData.applySync(activeSchool, schoolLevels, unlockedSchools, learnedSpells);
    }

    private static void handleCastResult(boolean success, String schoolId, String spellId,
                                          String reasonKey, int cooldownTicks) {
        if (success) {
            ClientSchoolData.noteServerCastSuccess(schoolId, spellId, cooldownTicks);
            SihriyaUiSounds.success();
        } else {
            SihriyaNotifications.castBlocked(schoolId, reasonKey);
            ClientSchoolData.clearPendingCast();
        }
    }

    private static void handleSpellParticles(String spellId, String schoolId) {
        // Only fire school particles at player position — no more circle there
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
    }

    private static void handleVFXTrigger(String spellId, String schoolId,
                                           int entityId, double x, double y, double z) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var level = (ClientLevel) mc.level;
        Vec3 pos = new Vec3(x, y + 0.5, z);

        boolean isBlazingSun = "fire.blazing_sun".equals(spellId);

        // entityId == -1 means the projectile exploded → cleanup sun circles
        if (isBlazingSun && entityId == -1) {
            ACTIVE_CIRCLES.values().removeIf(c -> c.schoolId.equals(schoolId) && c.maxRadius >= 20);
            return;
        }

        // Determine spell type for visual customization
        var spellData = SpellRegistry.get(spellId);
        var type = spellData != null ? spellData.type : null;
        boolean hasProjectileVisual = type == SpellRegistry.SpellType.PROJECTILE;

        // Circle parameters based on spell type
        int circleTicks;
        double circleRadius;

        if (isBlazingSun) {
            circleTicks = 1500;
            circleRadius = 25.0;
        } else if (type == SpellRegistry.SpellType.ULTIMATE) {
            circleTicks = 200;
            circleRadius = 15.0;
        } else if (type == SpellRegistry.SpellType.ZONE) {
            circleTicks = 300;
            circleRadius = 8.0;
        } else if (type == SpellRegistry.SpellType.PROJECTILE) {
            circleTicks = 120;
            circleRadius = 5.0;
        } else {
            circleTicks = 80;
            circleRadius = 4.0;
        }

        // Cercle magique visible seulement pour T4/T5 (ou blazing_sun)
        int tier = spellData != null ? spellData.tier : 0;
        if (isBlazingSun || tier >= 4) {
            ACTIVE_CIRCLES.put(nextCircleId++, new ActiveCircle(level, pos, schoolId, circleTicks, circleRadius));
        }

        // Impact particles
        if (SihriyaClientConfig.VFX_BLOOM.get()) {
            ImpactHandler.spawnImpact(level, pos, schoolId);
        }

        // VFX definition — skip projectile mesh for non-PROJECTILE types
        VFXDefinition def = VFXRegistry.get(spellId, schoolId);
        if (def != null) {
            VFXEffect effect = VFXFactory.createEffect(def, level, pos, schoolId, hasProjectileVisual);
            effect.setVfxId(spellId);
            VFXEngine.getInstance().startEffect(effect);

            // Screen shake from impact config
            if (def.impact() != null && def.impact().screenShake()) {
                ScreenShakeHandler.trigger(def.impact().screenShakeIntensity(), 12);
            }
        }
    }

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(Sihriya.MODID, path);
    }
}
