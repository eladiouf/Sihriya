package tong.sihriya.compat;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import tong.sihriya.Sihriya;
import tong.sihriya.animation.SihriyaAnimationPlayer;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.network.SpellParticlePacket;

public class IronsSpellsCompat {
    private static boolean detected = false;

    public static void init() {
        try {
            Class.forName("io.redspace.ironsspellbooks.IronsSpellbooks");
            detected = true;
            Sihriya.LOGGER.info("Iron's Spells 'n Spellbooks détecté ! Compatibilité activée.");
            MinecraftForge.EVENT_BUS.register(new IronsSpellsCompat());
        } catch (ClassNotFoundException e) {
            detected = false;
            Sihriya.LOGGER.info("Iron's Spells 'n Spellbooks non détecté. Compatibilité désactivée.");
        }
    }

    public static boolean isDetected() {
        return detected;
    }

    @SubscribeEvent
    public void onSpellPreCast(io.redspace.ironsspellbooks.api.events.SpellPreCastEvent event) {
        if (!detected) return;
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)) return;

        String spellId = event.getSpellId();
        if (!spellId.startsWith(Sihriya.MODID + ":")) return;

        String bareId = spellId.substring(Sihriya.MODID.length() + 1);
        SihriyaAnimationPlayer.play(sp, bareId, SihriyaAnimationPlayer.SpellPhase.CHANT);
    }

    @SubscribeEvent
    public void onSpellCast(io.redspace.ironsspellbooks.api.events.SpellOnCastEvent event) {
        if (!detected) return;
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)) return;

        String spellId = event.getSpellId();
        if (!spellId.startsWith(Sihriya.MODID + ":")) return;

        String bareId = spellId.substring(Sihriya.MODID.length() + 1);
        SihriyaAnimationPlayer.play(sp, bareId, SihriyaAnimationPlayer.SpellPhase.CAST);

        var spell = tong.sihriya.data.SpellRegistry.get(bareId);
        if (spell != null) {
            NetworkHandler.sendToPlayer(new SpellParticlePacket(bareId, spell.school), sp);
        }
    }
}
