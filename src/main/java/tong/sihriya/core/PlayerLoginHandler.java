package tong.sihriya.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.network.*;

import java.util.*;

@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class PlayerLoginHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        serverPlayer.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).ifPresent(prog -> {
            // First join: give starting school + spells
            if (prog.getUnlockedSchools().isEmpty()) {
                initStartingSchools(serverPlayer, prog);
            }
            syncSchools(serverPlayer, prog);
        });

        serverPlayer.getCapability(ManaProvider.MANA).ifPresent(mana -> {
            NetworkHandler.sendToPlayer(
                new ManaSyncPacket(mana.getMana(), mana.getMaxMana(),
                    mana.isLocked(), mana.getLockRemainingMs()),
                serverPlayer);
        });
    }

    private static void initStartingSchools(ServerPlayer player, SchoolProgression prog) {
        var startingSchools = SchoolRegistry.getStartingSchools();
        if (startingSchools.isEmpty()) return;

        // Pick random starting school
        var rand = player.getRandom();
        var school = startingSchools.get(rand.nextInt(startingSchools.size()));
        prog.unlockSchool(school.id);
        prog.setActiveSchool(school.id);

        // Grant 2 random T1 spells from that school
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

    private static void syncSchools(ServerPlayer player, SchoolProgression prog) {
        NetworkHandler.sendToPlayer(new SchoolSyncPacket(
            prog.getActiveSchool(),
            new HashMap<>(),
            prog.getUnlockedSchools(),
            prog.getLearnedSpells()
        ), player);
    }
}
