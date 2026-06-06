package tong.sihriya.client.vfx;

import net.minecraft.client.Minecraft;
import tong.sihriya.Sihriya;
import tong.sihriya.client.vfx.post.BloomPostChain;

public class PerformanceMonitor {
    private static final int CHECK_INTERVAL = 100;
    private static final int LOW_FPS_THRESHOLD = 30;
    private int tickCounter;
    private float fpsSum;

    public void tick() {
        tickCounter++;
        fpsSum += Minecraft.getInstance().getFps();
        if (tickCounter >= CHECK_INTERVAL) {
            float avgFps = fpsSum / CHECK_INTERVAL;
            if (avgFps < LOW_FPS_THRESHOLD && BloomPostChain.isActive()) {
                BloomPostChain.setActive(false);
                Sihriya.LOGGER.info("Auto-disabled bloom: avg FPS {}", avgFps);
            }
            tickCounter = 0;
            fpsSum = 0;
        }
    }
}
