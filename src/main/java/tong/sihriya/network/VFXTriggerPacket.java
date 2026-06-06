package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VFXTriggerPacket {
    private final String spellId;
    private final String schoolId;
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;

    public VFXTriggerPacket(String spellId, String schoolId, int entityId,
                             double x, double y, double z) {
        this.spellId = spellId;
        this.schoolId = schoolId;
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(VFXTriggerPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.spellId, NetworkInputRules.MAX_SPELL_ID_LENGTH);
        buf.writeUtf(packet.schoolId, NetworkInputRules.MAX_SCHOOL_ID_LENGTH);
        buf.writeInt(packet.entityId);
        buf.writeDouble(packet.x);
        buf.writeDouble(packet.y);
        buf.writeDouble(packet.z);
    }

    public static VFXTriggerPacket decode(FriendlyByteBuf buf) {
        return new VFXTriggerPacket(
            buf.readUtf(NetworkInputRules.MAX_SPELL_ID_LENGTH),
            buf.readUtf(NetworkInputRules.MAX_SCHOOL_ID_LENGTH),
            buf.readInt(),
            buf.readDouble(), buf.readDouble(), buf.readDouble()
        );
    }

    public static void handle(VFXTriggerPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketBridge.handleVFXTrigger(packet.spellId, packet.schoolId,
                packet.entityId, packet.x, packet.y, packet.z);
        });
        ctx.get().setPacketHandled(true);
    }
}
