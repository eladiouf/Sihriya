package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpellParticlePacket {
    private final String spellId;
    private final String schoolId;

    public SpellParticlePacket(String spellId, String schoolId) {
        this.spellId = spellId;
        this.schoolId = schoolId;
    }

    public static void encode(SpellParticlePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.spellId, NetworkInputRules.MAX_SPELL_ID_LENGTH);
        buf.writeUtf(packet.schoolId, NetworkInputRules.MAX_SCHOOL_ID_LENGTH);
    }

    public static SpellParticlePacket decode(FriendlyByteBuf buf) {
        return new SpellParticlePacket(
            buf.readUtf(NetworkInputRules.MAX_SPELL_ID_LENGTH),
            buf.readUtf(NetworkInputRules.MAX_SCHOOL_ID_LENGTH)
        );
    }

    public static void handle(SpellParticlePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketBridge.handleSpellParticles(packet.spellId, packet.schoolId);
        });
        ctx.get().setPacketHandled(true);
    }
}
