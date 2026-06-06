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
    public static int getUnlockLevel(int tier) {
        return ProgressionUnlockRules.unlockLevelForTier(tier);
    }

    /** Vérifie les déblocages pour un joueur. Appelé après chaque gain d'XP d'école. */
    public static void checkUnlocks(ServerPlayer player, SchoolProgression prog) {
        boolean changed = false;

        var spellUnlocks = ProgressionUnlockRules.newlyUnlockedSpellIds(
            SpellRegistry.getAll(), prog::getLevel, prog.getLearnedSpells());
        for (String spellId : spellUnlocks) {
            var spell = SpellRegistry.get(spellId);
            prog.learnSpell(spellId);
            changed = true;
            Sihriya.LOGGER.debug("Sort débloqué: {} (tier {}, niveau {})",
                spellId, spell.tier, ProgressionUnlockRules.unlockLevelForTier(spell.tier));
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
        var schoolUnlocks = ProgressionUnlockRules.newlyUnlockedSchoolIds(
            SchoolRegistry.getAll(), prog::getLevel, prog.getUnlockedSchools());
        for (String schoolId : schoolUnlocks) {
            prog.unlockSchool(schoolId);
            // Apprendre le premier sort T1 de la nouvelle école
            var t1Spells = SpellRegistry.getBySchoolAndTier(schoolId, 1);
            if (!t1Spells.isEmpty()) {
                prog.learnSpell(t1Spells.get(0).id);
            }
            changed = true;
            Sihriya.LOGGER.info("École débloquée: {} pour {}", schoolId, player.getName().getString());
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
