package tong.sihriya.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.GraphicsStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
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
import tong.sihriya.config.SihriyaClientConfig;
import tong.sihriya.data.SchoolColors;
import tong.sihriya.registry.SihriyaEntities;
import tong.sihriya.registry.SihriyaParticles;
import tong.sihriya.client.vfx.VFXEngine;
import tong.sihriya.client.vfx.data.SihriyaVFXData;
import tong.sihriya.client.vfx.post.BloomPostChain;
import tong.sihriya.client.vfx.render.ProceduralTextureHelper;
import tong.sihriya.client.vfx.shader.SihriyaCoreShaders;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientPacketHandlers.register();
        event.enqueueWork(() -> {
            SpellIconRenderer.init();
            BloomPostChain.init(Minecraft.getInstance());
            ProceduralTextureHelper.generate();
            VFXEngine.init();
        });
        MinecraftForge.EVENT_BUS.register(ManaOverlay.INSTANCE);
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
            if (e.phase == TickEvent.Phase.END) VFXEngine.getInstance().tick();
        });
        MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent e) -> {
            if (e.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
                VFXEngine.getInstance().render(e.getPoseStack(), e.getPartialTick());
                if (isFabulous() || (isFancy() && SihriyaClientConfig.VFX_BLOOM.get())) {
                    BloomPostChain.processFrame(e.getPartialTick());
                }
            }
        });
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
            s -> new SchoolGlowParticleProvider(s, SchoolColors.get("fire")));
        event.registerSpriteSet(SihriyaParticles.WATER_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, SchoolColors.get("water")));
        event.registerSpriteSet(SihriyaParticles.WIND_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, SchoolColors.get("wind")));
        event.registerSpriteSet(SihriyaParticles.EARTH_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, SchoolColors.get("earth")));
        event.registerSpriteSet(SihriyaParticles.LIGHTNING_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, SchoolColors.get("lightning")));
        event.registerSpriteSet(SihriyaParticles.ICE_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, SchoolColors.get("ice")));
        event.registerSpriteSet(SihriyaParticles.LAVA_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, SchoolColors.get("lava")));
        event.registerSpriteSet(SihriyaParticles.NECRO_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, SchoolColors.get("necromancy")));
        event.registerSpriteSet(SihriyaParticles.LUMI_GLOW.get(),
            s -> new SchoolGlowParticleProvider(s, SchoolColors.get("lumamancy")));
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        SihriyaCoreShaders.onRegisterShaders(event);
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SihriyaVFXData());
    }

    public static boolean isFabulous() {
        return Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FABULOUS;
    }

    public static boolean isFancy() {
        return Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FANCY;
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SihriyaEntities.MAGIC_CIRCLE.get(), MagicCircleRenderer::new);
        event.registerEntityRenderer(SihriyaEntities.SPELL_PROJECTILE.get(), SpellProjectileRenderer::new);
    }
}
