package tong.sihriya.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.client.gui.GrimoireScreen;
import tong.sihriya.client.gui.SihriyaNotifications;
import tong.sihriya.client.gui.SihriyaUiSounds;
import tong.sihriya.client.gui.SpellWheelScreen;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.network.SchoolCastPacket;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Sihriya.MODID, value = Dist.CLIENT)
public class SchoolKeyHandler {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        boolean shift = Screen.hasShiftDown();

        // Keys 1-6: try hotbar first, fall back to schools
        for (int i = 0; i < KeyBindings.ALL_SCHOOLS.length; i++) {
            if (KeyBindings.ALL_SCHOOLS[i].consumeClick()) {
                String spellId = ClientSchoolData.getHotbarSpell(i);
                if (spellId != null && !spellId.isEmpty()) {
                    // Cast specific hotbar spell
                    ClientSchoolData.noteHotbarCast(i, spellId);
                    NetworkHandler.CHANNEL.sendToServer(new SchoolCastPacket(spellId));
                } else {
                    // Fall back to school-based casting
                    String schoolId;
                    if (shift && i < KeyBindings.ADVANCED_SCHOOL_IDS.length) {
                        schoolId = KeyBindings.ADVANCED_SCHOOL_IDS[i];
                    } else {
                        schoolId = KeyBindings.SCHOOL_IDS[i];
                    }
                    var reason = ClientSchoolData.getClientCastBlockReason(schoolId);
                    if (reason.isPresent()) {
                        SihriyaNotifications.castBlocked(schoolId, reason.get());
                        continue;
                    }
                    ClientSchoolData.noteCastRequest(schoolId);
                    NetworkHandler.CHANNEL.sendToServer(new SchoolCastPacket(schoolId));
                }
            }
        }

        if (KeyBindings.SPELL_WHEEL.consumeClick()) {
            SihriyaUiSounds.open();
            mc.setScreen(new SpellWheelScreen());
        }

        if (KeyBindings.GRIMOIRE.consumeClick()) {
            SihriyaUiSounds.open();
            mc.setScreen(new GrimoireScreen());
        }
    }
}
