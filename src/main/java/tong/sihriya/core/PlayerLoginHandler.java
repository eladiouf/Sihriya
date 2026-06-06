package tong.sihriya.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.integration.STATModIntegration;
import tong.sihriya.network.*;
import tong.statmod.stats.StatType;

import java.util.*;

@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class PlayerLoginHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        syncPlayer(serverPlayer);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncPlayer(serverPlayer);
        }
    }

    public static void syncPlayer(ServerPlayer player) {
        player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).ifPresent(prog -> {
            // First join: give starting school + spells
            if (prog.getUnlockedSchools().isEmpty()) {
                initStartingSchools(player, prog);
            }
            syncSchools(player, prog);
        });

        player.getCapability(ManaProvider.MANA).ifPresent(mana -> {
            NetworkHandler.sendToPlayer(
                new ManaSyncPacket(mana.getMana(player), mana.getMaxMana(player),
                    mana.isLocked(player), mana.getLockRemainingMs(player)),
                player);
        });
    }

    private static void initStartingSchools(ServerPlayer player, SchoolProgression prog) {
        var startingSchools = SchoolRegistry.getStartingSchools();
        if (startingSchools.isEmpty()) return;

        // Choisir l'école basée sur la stat STAT Mod la plus haute
        var school = pickSchoolByHighestStat(player, startingSchools);
        prog.unlockSchool(school.id);
        prog.setActiveSchool(school.id);

        // Grant 2 random T1 spells from that school
        var rand = player.getRandom();
        var t1Spells = tong.sihriya.data.SpellRegistry.getBySchoolAndTier(school.id, 1);
        if (!t1Spells.isEmpty()) {
            var shuffled = new ArrayList<>(t1Spells);
            Collections.shuffle(shuffled, new Random(rand.nextLong()));
            int count = Math.min(2, shuffled.size());
            for (int i = 0; i < count; i++) {
                prog.learnSpell(shuffled.get(i).id);
            }
        }

        Sihriya.LOGGER.info("Player {} started with school: {}", player.getName().getString(), school.id);
    }

    /** Choisit l'école de départ basée sur la stat d'affinité la plus haute. */
    private static SchoolRegistry.SchoolData pickSchoolByHighestStat(
            ServerPlayer player, List<SchoolRegistry.SchoolData> startingSchools) {
        // Mapper les stats d'affinité vers les schoolIds
        Map<StatType, String> statToSchool = Map.of(
            StatType.FIRE_AFFINITY, "fire",
            StatType.WATER_AFFINITY, "water",
            StatType.AIR_AFFINITY, "wind",
            StatType.EARTH_AFFINITY, "earth"
        );

        // Trouver la stat la plus haute
        int highestLevel = -1;
        List<String> bestSchools = new ArrayList<>();
        for (var entry : statToSchool.entrySet()) {
            int level = STATModIntegration.getStatLevel(player, entry.getKey());
            if (level > highestLevel) {
                highestLevel = level;
                bestSchools.clear();
                bestSchools.add(entry.getValue());
            } else if (level == highestLevel) {
                bestSchools.add(entry.getValue());
            }
        }

        // Filtrer les écoles de départ disponibles
        var candidates = startingSchools.stream()
            .filter(s -> bestSchools.contains(s.id))
            .toList();

        if (!candidates.isEmpty()) {
            return candidates.get(player.getRandom().nextInt(candidates.size()));
        }

        // Fallback: aléatoire parmi les écoles de départ
        return startingSchools.get(player.getRandom().nextInt(startingSchools.size()));
    }

    private static void syncSchools(ServerPlayer player, SchoolProgression prog) {
        Map<String, Integer> levels = new HashMap<>();
        for (String schoolId : prog.getUnlockedSchools()) {
            levels.put(schoolId, prog.getLevel(schoolId));
        }
        NetworkHandler.sendToPlayer(new SchoolSyncPacket(
            prog.getActiveSchool(),
            levels,
            prog.getUnlockedSchools(),
            prog.getLearnedSpells()
        ), player);
    }
}
