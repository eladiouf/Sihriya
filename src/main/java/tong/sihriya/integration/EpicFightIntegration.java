package tong.sihriya.integration;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import tong.sihriya.Sihriya;
import tong.sihriya.animation.SihriyaAnimationPlayer;
import tong.sihriya.animation.SpellAnimationManager;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;

import java.nio.file.Path;

public class EpicFightIntegration {
    private static final String EPIC_FIGHT_FIX_PACK_ID = "sihriya_epicfight_data_fixes";
    private static final String EPIC_FIGHT_FIX_PACK_PATH = "packs/sihriya_epicfight_data_fixes";
    private static boolean bootstrapped = false;
    private static boolean initialized = false;

    public static void bootstrap(IEventBus modBus) {
        if (bootstrapped) return;
        ensureEpicFightPresent();

        modBus.addListener(EpicFightIntegration::onAddPackFinders);
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.AddReloadListenerEvent event) -> {
            event.addListener(new tong.sihriya.animation.SpellAnimationLoader());
        });

        bootstrapped = true;
    }

    public static void init() {
        if (initialized) return;
        ensureEpicFightPresent();
        SpellAnimationManager.registerDefaults();
        Sihriya.LOGGER.info("Epic Fight détecté et intégré !");
        initialized = true;
    }

    public static boolean isInitialized() { return initialized; }

    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;

        Path packPath = ModList.get().getModFileById(Sihriya.MODID)
            .getFile()
            .findResource("packs", EPIC_FIGHT_FIX_PACK_ID);

        Pack pack = Pack.readMetaAndCreate(
            EPIC_FIGHT_FIX_PACK_ID,
            Component.literal("Sihriya Epic Fight data fixes"),
            true,
            name -> new PathPackResources(name, packPath, false),
            PackType.SERVER_DATA,
            Pack.Position.TOP,
            PackSource.BUILT_IN
        );

        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        } else {
            Sihriya.LOGGER.warn("Unable to create built-in data pack {}", EPIC_FIGHT_FIX_PACK_PATH);
        }
    }

    private static void ensureEpicFightPresent() {
        try {
            Class.forName("yesman.epicfight.main.EpicFightMod");
        } catch (ClassNotFoundException e) {
            Sihriya.LOGGER.error("Epic Fight est requis mais introuvable !");
            throw new RuntimeException("Epic Fight manquant", e);
        }
    }

    /** Joue l'animation d'un sort sur un joueur */
    public static void playSpellAnimation(ServerPlayer player, String spellId) {
        if (!initialized) return;
        SpellData spell = SpellRegistry.get(spellId);
        if (spell == null) return;

        // Calcul du temps d'incantation réduit par CASTING_SPEED
        int castTime = STATModIntegration.getCastTime(player, spell);
        Sihriya.LOGGER.debug("Spell {} cast in {} ticks (reduced by CASTING_SPEED)", spellId, castTime);

        SihriyaAnimationPlayer.play(player, spellId, SihriyaAnimationPlayer.SpellPhase.CAST);
    }
}
