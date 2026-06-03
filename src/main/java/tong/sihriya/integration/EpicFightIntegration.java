package tong.sihriya.integration;

import net.minecraft.server.level.ServerPlayer;
import tong.sihriya.Sihriya;
import tong.sihriya.animation.SpellAnimationManager;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;

public class EpicFightIntegration {
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        try {
            Class.forName("yesman.epicfight.main.EpicFightMod");
            SpellAnimationManager.registerDefaults();

            // Enregistrer le SpellAnimationLoader (reload listener)
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
                (net.minecraftforge.event.AddReloadListenerEvent event) -> {
                    event.addListener(new tong.sihriya.animation.SpellAnimationLoader());
                });

            Sihriya.LOGGER.info("Epic Fight détecté et intégré !");
            initialized = true;
        } catch (ClassNotFoundException e) {
            Sihriya.LOGGER.error("Epic Fight est requis mais introuvable !");
            throw new RuntimeException("Epic Fight manquant");
        }
    }

    public static boolean isInitialized() { return initialized; }

    /** Joue l'animation d'un sort sur un joueur */
    public static void playSpellAnimation(ServerPlayer player, String spellId) {
        if (!initialized) return;
        SpellData spell = SpellRegistry.get(spellId);
        if (spell == null) return;

        // Calcul du temps d'incantation réduit par CASTING_SPEED
        int castTime = STATModIntegration.getCastTime(player, spell);
        Sihriya.LOGGER.debug("Spell {} cast in {} ticks (reduced by CASTING_SPEED)", spellId, castTime);

        // TODO: jouer l'animation Epic Fight quand l'API sera intégrée
        // ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        // if (playerPatch != null) { playerPatch.playAnimation(...); }
    }
}
