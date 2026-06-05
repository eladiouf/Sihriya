package tong.sihriya.client;

import net.minecraft.client.Minecraft;
import tong.sihriya.client.gui.SihriyaNotifications;
import tong.sihriya.client.gui.SihriyaUiSounds;
import tong.sihriya.client.particle.SpellParticleHelper;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.network.ClientPacketBridge;

import java.util.Map;
import java.util.Set;

public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void register() {
        ClientPacketBridge.registerHandlers(
            ClientPacketHandlers::handleManaSync,
            ClientPacketHandlers::handleSchoolSync,
            ClientPacketHandlers::handleCastResult,
            ClientPacketHandlers::handleSpellParticles
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
        if (mc.player == null) return;

        var spell = SpellRegistry.get(spellId);
        int duration = 60;
        if (spell != null) {
            duration = Math.max(60, spell.castTime + 40);
        }

        SpellParticleHelper.spawnCircleAround(mc.player, schoolId, duration);
        SpellParticleHelper.spawnGlowBurst(
            mc.player.level(),
            mc.player.position().add(0, 1, 0),
            schoolId,
            10
        );
    }
}
