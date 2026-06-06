package tong.sihriya.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import tong.sihriya.client.gui.SihriyaNotifications;
import tong.sihriya.client.gui.SihriyaUiSounds;
import tong.sihriya.client.particle.SpellParticleHelper;
import tong.sihriya.magiccircle.MagicCircleEntity;
import tong.sihriya.registry.SihriyaEntities;
import tong.sihriya.client.vfx.VFXEffect;
import tong.sihriya.client.vfx.VFXEngine;
import tong.sihriya.client.vfx.render.ImpactHandler;
import tong.sihriya.client.vfx.render.VFXFactory;
import tong.sihriya.config.SihriyaClientConfig;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;
import tong.sihriya.network.ClientPacketBridge;
import tong.sihriya.vfx.VFXDefinition;
import tong.sihriya.vfx.VFXRegistry;

import java.util.Map;
import java.util.Set;

public final class ClientPacketHandlers {
    private ClientPacketHandlers() {}

    public static void register() {
        ClientPacketBridge.registerHandlers(
            ClientPacketHandlers::handleManaSync,
            ClientPacketHandlers::handleSchoolSync,
            ClientPacketHandlers::handleCastResult,
            ClientPacketHandlers::handleSpellParticles,
            ClientPacketHandlers::handleVFXTrigger
        );
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
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        SpellData spell = SpellRegistry.get(spellId);
        int duration = spell != null ? Math.max(60, spell.castTime + 40) : 60;
        SpellParticleHelper.spawnCircleAround(mc.player, schoolId, duration);
    }

    private static void handleVFXTrigger(String spellId, String schoolId,
                                           int entityId, double x, double y, double z) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var level = (ClientLevel) mc.level;
        Vec3 pos = new Vec3(x, y, z);

        // Magic circle
        MagicCircleEntity circle = new MagicCircleEntity(
            SihriyaEntities.MAGIC_CIRCLE.get(), level, schoolId, 60);
        circle.setPos(x, y, z);
        level.addFreshEntity(circle);

        // Impact particles
        if (SihriyaClientConfig.VFX_BLOOM.get()) {
            ImpactHandler.spawnImpact(level, pos, schoolId);
        }

        // VFX definition
        VFXDefinition def = VFXRegistry.get(spellId, schoolId);
        if (def != null) {
            VFXEffect effect = VFXFactory.createEffect(def, level, pos, schoolId);
            effect.setVfxId(spellId);
            VFXEngine.getInstance().startEffect(effect);
        }
    }
}
