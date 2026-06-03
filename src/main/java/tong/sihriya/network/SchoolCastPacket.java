package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tong.sihriya.core.SpellCastHandler;

import java.util.function.Supplier;

public class SchoolCastPacket {
    private final String schoolId;

    public SchoolCastPacket(String schoolId) {
        this.schoolId = schoolId;
    }

    public static void encode(SchoolCastPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.schoolId);
    }

    public static SchoolCastPacket decode(FriendlyByteBuf buf) {
        return new SchoolCastPacket(buf.readUtf());
    }

    public static void handle(SchoolCastPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                var result = SpellCastHandler.castBySchoolDetailed(player, packet.schoolId);
                NetworkHandler.sendToPlayer(new CastResultPacket(result.success(), result.schoolId(),
                    result.spellId(), result.reasonKey(), result.cooldownTicks()), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
