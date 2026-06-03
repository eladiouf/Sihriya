package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import tong.sihriya.Sihriya;
import tong.sihriya.client.particle.SpellParticleHelper;
import tong.sihriya.data.SpellRegistry;

import java.util.function.Supplier;

public class SpellParticlePacket {
    private final String spellId;
    private final String schoolId;

    public SpellParticlePacket(String spellId, String schoolId) {
        this.spellId = spellId;
        this.schoolId = schoolId;
    }

    public static void encode(SpellParticlePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.spellId);
        buf.writeUtf(packet.schoolId);
    }

    public static SpellParticlePacket decode(FriendlyByteBuf buf) {
        return new SpellParticlePacket(buf.readUtf(), buf.readUtf());
    }

    public static void handle(SpellParticlePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleParticles(packet);
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleParticles(SpellParticlePacket packet) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return;

        var spell = SpellRegistry.get(packet.spellId);
        int duration = 40; // default ~2s
        if (spell != null) {
            duration = Math.max(20, spell.castTime + 30);
        }

        // Cercle magique autour du joueur
        SpellParticleHelper.spawnCircleAround(mc.player, packet.schoolId, duration);

        // Rafale de particules glow
        SpellParticleHelper.spawnGlowBurst(
            mc.player.level(),
            mc.player.position().add(0, 1, 0),
            packet.schoolId,
            10
        );
    }
}
