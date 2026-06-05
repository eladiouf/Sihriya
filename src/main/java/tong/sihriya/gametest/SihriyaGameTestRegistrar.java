package tong.sihriya.gametest;

import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SihriyaGameTestRegistrar {
    private static final String GAME_TEST_CLASS = "tong.sihriya.gametest.SihriyaRuntimeGameTests";
    private static final String GAME_TEST_METHOD = "coreRegistriesAndFlowBootInGame";

    private SihriyaGameTestRegistrar() {}

    @SubscribeEvent
    public static void registerGameTests(RegisterGameTestsEvent event) {
        try {
            Class<?> testClass = Class.forName(GAME_TEST_CLASS);
            Method testMethod = testClass.getDeclaredMethod(GAME_TEST_METHOD, net.minecraft.gametest.framework.GameTestHelper.class);
            event.register(testMethod);
            Sihriya.LOGGER.info("Registered GameTest method {}#{}", GAME_TEST_CLASS, GAME_TEST_METHOD);
        } catch (ClassNotFoundException ignored) {
            // The runtime test class lives in src/test/java and is only present in GameTest runs.
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unable to register GameTest method " + GAME_TEST_CLASS + "#" + GAME_TEST_METHOD, e);
        }
    }
}
