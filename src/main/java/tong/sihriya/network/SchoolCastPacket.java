package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tong.sihriya.core.SpellCastHandler;
import tong.sihriya.data.SchoolRegistry;

import java.util.function.Supplier;

public class SchoolCastPacket {
    private final String schoolId;

    public SchoolCastPacket(String schoolId) {
        this.schoolId = schoolId;
    }

    public static void encode(SchoolCastPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.schoolId, NetworkInputRules.MAX_SCHOOL_ID_LENGTH);
    }

    public static SchoolCastPacket decode(FriendlyByteBuf buf) {
        return new SchoolCastPacket(buf.readUtf(NetworkInputRules.MAX_SCHOOL_ID_LENGTH));
    }

    public static void handle(SchoolCastPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                var result = NetworkInputRules.isValidSchoolId(packet.schoolId, id -> SchoolRegistry.get(id) != null)
                    ? SpellCastHandler.castBySchoolDetailed(player, packet.schoolId)
                    : SpellCastHandler.CastResult.fail("", "", "notification.sihriya.cast_no_spell");
                NetworkHandler.sendToPlayer(new CastResultPacket(result.success(), result.schoolId(),
                    result.spellId(), result.reasonKey(), result.cooldownTicks()), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
