package tong.sihriya.epicfight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.integration.STATModIntegration;

/**
 * Passe les bonus de dégâts STAT Mod à travers les dégâts Epic Fight.
 * Quand Epic Fight calcule des dégâts, ce handler applique le multiplicateur
 * d'affinité élémentaire de Sihriya.
 */
@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class StatPassthroughHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity target = event.getEntity();

        // Vérifier que le joueur a des stats Sihriya
        if (!STATModIntegration.isInitialized()) return;

        // Appliquer le multiplicateur d'affinité (moyenne de toutes les écoles débloquées)
        // On utilise un bonus global basé sur la moyenne des affinités
        float multiplier = calculateGlobalMultiplier(player);
        if (multiplier > 1.0f) {
            event.setAmount(event.getAmount() * multiplier);
        }

        // Appliquer la résistance magique de la cible si c'est un joueur
        if (target instanceof ServerPlayer targetPlayer) {
            float resistance = STATModIntegration.getMagicResistance(targetPlayer);
            if (resistance > 0) {
                event.setAmount(event.getAmount() * (1 - resistance));
            }
        }
    }

    /** Calcule un multiplicateur global basé sur toutes les affinités du joueur. */
    private static float calculateGlobalMultiplier(ServerPlayer player) {
        // Moyenne des multiplicateurs de toutes les écoles
        String[] schools = {"fire", "water", "wind", "earth", "lightning", "ice", "lava", "necromancy", "lumamancy"};
        float total = 0;
        int count = 0;
        for (String school : schools) {
            float mult = STATModIntegration.getDamageMultiplier(player, school);
            if (mult > 1.0f) {
                total += mult;
                count++;
            }
        }
        return count > 0 ? total / count : 1.0f;
    }
}
