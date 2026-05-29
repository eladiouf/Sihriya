package tong.sihriya.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.client.gui.SpellWheelScreen;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, value = Dist.CLIENT)
public class SpellWheelInputHandler {
    private boolean wheelWasPressed = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean isPressed = KeyBindings.SPELL_WHEEL.isDown();

        if (isPressed && !wheelWasPressed) {
            // Open wheel on first press
            if (mc.screen == null) {
                mc.setScreen(new SpellWheelScreen());
            }
        }

        if (!isPressed && wheelWasPressed && mc.screen instanceof SpellWheelScreen wheel) {
            // On release: if a spell is selected, cast it
            if (wheel.getSelectedSpell() != null) {
                // TODO: send cast packet to server
                Sihriya.LOGGER.debug("Cast spell: {}", wheel.getSelectedSpell());
            }
            mc.setScreen(null);
        }

        wheelWasPressed = isPressed;
    }
}
