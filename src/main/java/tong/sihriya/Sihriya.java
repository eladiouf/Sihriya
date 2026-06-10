package tong.sihriya;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tong.sihriya.command.SihriyaCommand;
import tong.sihriya.config.SihriyaClientConfig;
import tong.sihriya.data.DataLoader;
import tong.sihriya.integration.EpicFightIntegration;
import tong.sihriya.integration.STATModIntegration;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.registry.SihriyaEntities;
import tong.sihriya.registry.SihriyaParticles;

@Mod(Sihriya.MODID)
public class Sihriya {
    public static final String MODID = "sihriya";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public Sihriya(FMLJavaModLoadingContext context) {
        var bus = context.getModEventBus();
        bus.addListener(this::commonSetup);
        EpicFightIntegration.bootstrap(bus);
        context.registerConfig(ModConfig.Type.CLIENT, SihriyaClientConfig.SPEC);
        tong.sihriya.core.ModEffects.EFFECTS.register(bus);
        SihriyaParticles.PARTICLES.register(bus);
        SihriyaEntities.ENTITIES.register(bus);

        // Iron's Spellbooks integration (optionnel)
        try {
            Class.forName("tong.sihriya.compat.SihriyaIronsSchools");
            tong.sihriya.compat.SihriyaIronsSchools.register(bus);
            tong.sihriya.compat.SihriyaIronsSpellRegister.register(bus);
            LOGGER.info("Iron's Spellbooks integration activée");
        } catch (Throwable e) {
            LOGGER.info("Iron's Spellbooks non détecté — fonctionnement autonome");
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DataLoader.loadAll();
            NetworkHandler.register();
            STATModIntegration.init();
            EpicFightIntegration.init();
            try {
                tong.sihriya.compat.IronsSpellsCompat.init();
            } catch (Throwable e) {
                // IS integration non disponible
            }
            LOGGER.info("Sihriya chargé avec Epic Fight + STAT Mod !");
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SihriyaCommand.register(event.getDispatcher());
        LOGGER.info("Sihriya commands registered");
    }
}
