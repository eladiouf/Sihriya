package tong.sihriya.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.network.SchoolCastPacket;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Sihriya.MODID, value = Dist.CLIENT)
public class SchoolKeyHandler {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        boolean shift = Screen.hasShiftDown();

        for (int i = 0; i < KeyBindings.ALL_SCHOOLS.length; i++) {
            if (KeyBindings.ALL_SCHOOLS[i].consumeClick()) {
                String schoolId;
                if (shift && i < KeyBindings.ADVANCED_SCHOOL_IDS.length) {
                    // Shift+1/2/3 → écoles avancées (Lave, Nécromancie, Lumagie)
                    schoolId = KeyBindings.ADVANCED_SCHOOL_IDS[i];
                } else {
                    schoolId = KeyBindings.SCHOOL_IDS[i];
                }
                NetworkHandler.CHANNEL.sendToServer(new SchoolCastPacket(schoolId));
            }
        }
    }
}
