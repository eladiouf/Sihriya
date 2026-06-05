package tong.sihriya.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import tong.sihriya.Sihriya;
import tong.sihriya.client.gui.ClientUiOptions;
import tong.sihriya.config.SihriyaClientConfig;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientConfigEvents {
    private ClientConfigEvents() {
    }

    @SubscribeEvent
    public static void onLoadOrReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SihriyaClientConfig.SPEC) {
            ClientUiOptions.loadFromConfig();
        }
    }
}
