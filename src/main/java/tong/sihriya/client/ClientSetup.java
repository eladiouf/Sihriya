package tong.sihriya.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tong.sihriya.Sihriya;
import tong.sihriya.client.gui.ManaOverlay;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(ManaOverlay.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new SpellWheelInputHandler());
    }

    @SubscribeEvent
    public static void onKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.SPELL_WHEEL);
        event.register(KeyBindings.MEDITATE);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "sihriya_mana", ManaOverlay.INSTANCE);
    }
}
