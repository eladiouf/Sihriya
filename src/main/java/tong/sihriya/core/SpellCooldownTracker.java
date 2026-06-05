package tong.sihriya.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SpellCooldownTracker {
    private final Map<UUID, Map<String, Long>> cooldownsByPlayer = new HashMap<>();

    public int remainingCooldownTicks(UUID playerId, String spellId, int cooldownTicks, long nowMs) {
        Map<String, Long> playerCooldowns = cooldownsByPlayer.get(playerId);
        if (playerCooldowns == null) return 0;

        Long lastCastMs = playerCooldowns.get(spellId);
        if (lastCastMs == null) return 0;

        long remainingMs = cooldownTicks * 50L - (nowMs - lastCastMs);
        if (remainingMs <= 0) {
            playerCooldowns.remove(spellId);
            if (playerCooldowns.isEmpty()) {
                cooldownsByPlayer.remove(playerId);
            }
            return 0;
        }

        return (int) Math.ceil(remainingMs / 50.0);
    }

    public void recordCast(UUID playerId, String spellId, long castTimeMs) {
        cooldownsByPlayer.computeIfAbsent(playerId, ignored -> new HashMap<>()).put(spellId, castTimeMs);
    }

    public void clearPlayer(UUID playerId) {
        cooldownsByPlayer.remove(playerId);
    }

    int trackedPlayerCount() {
        return cooldownsByPlayer.size();
    }
}
