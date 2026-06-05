package tong.sihriya.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tong.sihriya.Sihriya;
import tong.sihriya.client.gui.ActiveSpellHud;
import tong.sihriya.client.gui.ManaOverlay;
import tong.sihriya.client.gui.NotificationOverlay;
import tong.sihriya.client.gui.SpellIconRenderer;
import tong.sihriya.client.particle.SchoolGlowParticleProvider;
import tong.sihriya.client.particle.magiccircle.MagicCircleRenderer;
import tong.sihriya.client.projectile.SpellProjectileRenderer;
import tong.sihriya.registry.SihriyaEntities;
import tong.sihriya.registry.SihriyaParticles;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientPacketHandlers.register();
        event.enqueueWork(SpellIconRenderer::init);
        MinecraftForge.EVENT_BUS.register(ManaOverlay.INSTANCE);
    }

    @SubscribeEvent
    public static void onKeyMappings(RegisterKeyMappingsEvent event) {
        for (var key : KeyBindings.ALL_SCHOOLS) {
            event.register(key);
        }
        event.register(KeyBindings.MEDITATE);
        event.register(KeyBindings.GRIMOIRE);
        event.register(KeyBindings.SPELL_WHEEL);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "sihriya_mana", ManaOverlay.INSTANCE);
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "sihriya_active_spell", ActiveSpellHud.INSTANCE);
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "sihriya_notifications", NotificationOverlay.INSTANCE);
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(SihriyaParticles.FIRE_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, 1.0f, 0.4f, 0.1f));
        event.registerSpriteSet(SihriyaParticles.WATER_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, 0.2f, 0.6f, 1.0f));
        event.registerSpriteSet(SihriyaParticles.WIND_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, 0.9f, 0.9f, 1.0f));
        event.registerSpriteSet(SihriyaParticles.EARTH_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, 0.3f, 0.7f, 0.2f));
        event.registerSpriteSet(SihriyaParticles.LIGHTNING_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, 1.0f, 0.9f, 0.1f));
        event.registerSpriteSet(SihriyaParticles.ICE_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, 0.5f, 0.8f, 1.0f));
        event.registerSpriteSet(SihriyaParticles.LAVA_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, 1.0f, 0.2f, 0.0f));
        event.registerSpriteSet(SihriyaParticles.NECRO_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, 0.5f, 0.0f, 0.8f));
        event.registerSpriteSet(SihriyaParticles.LUMI_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, 1.0f, 0.85f, 0.4f));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SihriyaEntities.MAGIC_CIRCLE.get(), MagicCircleRenderer::new);
        event.registerEntityRenderer(SihriyaEntities.SPELL_PROJECTILE.get(), SpellProjectileRenderer::new);
    }
}
