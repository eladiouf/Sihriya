package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CastResultPacket {
    private final boolean success;
    private final String schoolId;
    private final String spellId;
    private final String reasonKey;
    private final int cooldownTicks;

    public CastResultPacket(boolean success, String schoolId, String spellId, String reasonKey, int cooldownTicks) {
        this.success = success;
        this.schoolId = schoolId;
        this.spellId = spellId;
        this.reasonKey = reasonKey;
        this.cooldownTicks = cooldownTicks;
    }

    public static void encode(CastResultPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.success);
        buf.writeUtf(packet.schoolId, NetworkInputRules.MAX_SCHOOL_ID_LENGTH);
        buf.writeUtf(packet.spellId, NetworkInputRules.MAX_SPELL_ID_LENGTH);
        buf.writeUtf(packet.reasonKey, NetworkInputRules.MAX_REASON_KEY_LENGTH);
        buf.writeInt(packet.cooldownTicks);
    }

    public static CastResultPacket decode(FriendlyByteBuf buf) {
        return new CastResultPacket(
            buf.readBoolean(),
            buf.readUtf(NetworkInputRules.MAX_SCHOOL_ID_LENGTH),
            buf.readUtf(NetworkInputRules.MAX_SPELL_ID_LENGTH),
            buf.readUtf(NetworkInputRules.MAX_REASON_KEY_LENGTH),
            buf.readInt()
        );
    }

    public static void handle(CastResultPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketBridge.handleCastResult(packet.success, packet.schoolId, packet.spellId,
                packet.reasonKey, packet.cooldownTicks);
        });
        ctx.get().setPacketHandled(true);
    }
}
