package tong.sihriya.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;

/**
 * Clic droit avec la main vide → lance le sort actif sélectionné.
 */
@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class RightClickCastHandler {
    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        tryCast(serverPlayer);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        // Only cast if main hand is empty
        if (!serverPlayer.getMainHandItem().isEmpty()) return;
        tryCast(serverPlayer);
    }

    private static void tryCast(ServerPlayer player) {
        player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).ifPresent(prog -> {
            String activeSchool = prog.getActiveSchool();
            if (activeSchool.isEmpty()) return;

            // Find first T1 spell learned for this school
            var spells = prog.getLearnedSpells().stream()
                .filter(s -> s.startsWith(activeSchool + "."))
                .toList();
            if (spells.isEmpty()) return;

            String spellId = spells.get(0); // Cast first available spell
            SpellCastHandler.castSpell(player, spellId);
        });
    }
}
