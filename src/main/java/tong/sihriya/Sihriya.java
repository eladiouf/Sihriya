package tong.sihriya;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tong.sihriya.core.ManaManager;
import tong.sihriya.data.DataLoader;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.integration.SihriyaAPI;
import tong.sihriya.network.NetworkHandler;

@Mod(Sihriya.MODID)
public class Sihriya {
    public static final String MODID = "sihriya";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public Sihriya(FMLJavaModLoadingContext context) {
        var bus = context.getModEventBus();
        bus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DataLoader.loadAll();
            SihriyaAPI.detectStatMod();
            LOGGER.info("Sihriya chargé !");
        });
    }
}
