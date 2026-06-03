package tong.sihriya.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.network.SchoolSyncPacket;

import java.util.*;

@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class TierUnlockHandler {
    private static final int[] TIER_THRESHOLDS = {1, 25, 50, 75, 100};

    public static int getUnlockLevel(int tier) {
        if (tier < 1 || tier > 5) return 999;
        return TIER_THRESHOLDS[tier - 1];
    }

    /** Vérifie les déblocages pour un joueur. Appelé après chaque gain d'XP d'école. */
    public static void checkUnlocks(ServerPlayer player, SchoolProgression prog) {
        boolean changed = false;

        // Vérifier les paliers de sorts pour chaque école
        for (var school : SchoolRegistry.getAll()) {
            int level = prog.getLevel(school.id);
            for (int tier = 1; tier <= 5; tier++) {
                int threshold = TIER_THRESHOLDS[tier - 1];
                if (level >= threshold) {
                    // Débloquer les sorts de ce tier
                    var spells = SpellRegistry.getBySchoolAndTier(school.id, tier);
                    for (var spell : spells) {
                        if (!prog.isSpellLearned(spell.id)) {
                            prog.learnSpell(spell.id);
                            changed = true;
                            Sihriya.LOGGER.debug("Sort débloqué: {} (tier {}, niveau {})", spell.id, tier, threshold);
                        }
                    }
                }
            }
        }

        // Vérifier le déblocage des écoles avancées
        changed |= checkSchoolUnlocks(player, prog);

        // Sync si changement
        if (changed) {
            syncSchools(player, prog);
        }
    }

    /** Vérifie si de nouvelles écoles peuvent être débloquées. */
    private static boolean checkSchoolUnlocks(ServerPlayer player, SchoolProgression prog) {
        boolean changed = false;
        for (var school : SchoolRegistry.getAll()) {
            if (school.unlock == null) continue; // starting school
            if (prog.isSchoolUnlocked(school.id)) continue; // déjà débloquée

            if (school.unlock.isMet(prog)) {
                prog.unlockSchool(school.id);
                // Apprendre le premier sort T1 de la nouvelle école
                var t1Spells = SpellRegistry.getBySchoolAndTier(school.id, 1);
                if (!t1Spells.isEmpty()) {
                    prog.learnSpell(t1Spells.get(0).id);
                }
                changed = true;
                Sihriya.LOGGER.info("École débloquée: {} pour {}", school.id, player.getName().getString());
            }
        }
        return changed;
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
