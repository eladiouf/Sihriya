package tong.sihriya.client.vfx.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, value = Dist.CLIENT)
public final class ScreenShakeHandler {
    private static float intensity = 0;
    private static int remainingTicks = 0;
    private static final RandomSource RNG = RandomSource.create();

    public static void trigger(float strength, int durationTicks) {
        if (strength > intensity || remainingTicks <= 0) {
            intensity = Math.min(strength, 3.0f);
        }
        remainingTicks = Math.max(remainingTicks, durationTicks);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (remainingTicks <= 0) return;
        remainingTicks--;
        intensity *= 0.92f;
        if (intensity < 0.01f) {
            intensity = 0;
            remainingTicks = 0;
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (remainingTicks <= 0 || Minecraft.getInstance().player == null) return;
        float shakeX = (RNG.nextFloat() - 0.5f) * 2.0f * intensity;
        float shakeY = (RNG.nextFloat() - 0.5f) * 2.0f * intensity;
        event.setPitch(event.getPitch() + shakeX * 0.3f);
        event.setYaw(event.getYaw() + shakeY * 0.3f);
        event.setRoll(event.getRoll() + shakeX * shakeY * 0.2f);
    }

    private ScreenShakeHandler() {}
}
