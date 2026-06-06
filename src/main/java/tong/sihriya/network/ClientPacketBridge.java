package tong.sihriya.network;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ClientPacketBridge {
    private static volatile ManaSyncHandler manaSyncHandler = (mana, maxMana, locked, lockRemaining) -> {};
    private static volatile SchoolSyncHandler schoolSyncHandler = (activeSchool, schoolLevels, unlockedSchools, learnedSpells) -> {};
    private static volatile CastResultHandler castResultHandler = (success, schoolId, spellId, reasonKey, cooldownTicks) -> {};
    private static volatile SpellParticleHandler spellParticleHandler = (spellId, schoolId) -> {};
    private static volatile VFXTriggerHandler vfxTriggerHandler = (spellId, schoolId, entityId, x, y, z) -> {};

    private ClientPacketBridge() {
    }

    public static void registerHandlers(ManaSyncHandler manaSyncHandler,
                                        SchoolSyncHandler schoolSyncHandler,
                                        CastResultHandler castResultHandler,
                                        SpellParticleHandler spellParticleHandler,
                                        VFXTriggerHandler vfxTriggerHandler) {
        ClientPacketBridge.manaSyncHandler = Objects.requireNonNull(manaSyncHandler);
        ClientPacketBridge.schoolSyncHandler = Objects.requireNonNull(schoolSyncHandler);
        ClientPacketBridge.castResultHandler = Objects.requireNonNull(castResultHandler);
        ClientPacketBridge.spellParticleHandler = Objects.requireNonNull(spellParticleHandler);
        ClientPacketBridge.vfxTriggerHandler = Objects.requireNonNull(vfxTriggerHandler);
    }

    public static void handleManaSync(float mana, float maxMana, boolean locked, long lockRemaining) {
        manaSyncHandler.handle(mana, maxMana, locked, lockRemaining);
    }

    public static void handleSchoolSync(String activeSchool, Map<String, Integer> schoolLevels,
                                        Set<String> unlockedSchools, Set<String> learnedSpells) {
        schoolSyncHandler.handle(activeSchool, schoolLevels, unlockedSchools, learnedSpells);
    }

    public static void handleCastResult(boolean success, String schoolId, String spellId,
                                        String reasonKey, int cooldownTicks) {
        castResultHandler.handle(success, schoolId, spellId, reasonKey, cooldownTicks);
    }

    public static void handleSpellParticles(String spellId, String schoolId) {
        spellParticleHandler.handle(spellId, schoolId);
    }

    @FunctionalInterface
    public interface ManaSyncHandler {
        void handle(float mana, float maxMana, boolean locked, long lockRemaining);
    }

    @FunctionalInterface
    public interface SchoolSyncHandler {
        void handle(String activeSchool, Map<String, Integer> schoolLevels,
                    Set<String> unlockedSchools, Set<String> learnedSpells);
    }

    @FunctionalInterface
    public interface CastResultHandler {
        void handle(boolean success, String schoolId, String spellId, String reasonKey, int cooldownTicks);
    }

    @FunctionalInterface
    public interface SpellParticleHandler {
        void handle(String spellId, String schoolId);
    }

    @FunctionalInterface
    public interface VFXTriggerHandler {
        void handle(String spellId, String schoolId, int entityId, double x, double y, double z);
    }

    public static void handleVFXTrigger(String spellId, String schoolId,
                                         int entityId, double x, double y, double z) {
        vfxTriggerHandler.handle(spellId, schoolId, entityId, x, y, z);
    }
}
