package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tong.sihriya.client.ClientSchoolData;
import tong.sihriya.client.gui.SihriyaNotifications;
import tong.sihriya.client.gui.SihriyaUiSounds;

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
        buf.writeUtf(packet.schoolId);
        buf.writeUtf(packet.spellId);
        buf.writeUtf(packet.reasonKey);
        buf.writeInt(packet.cooldownTicks);
    }

    public static CastResultPacket decode(FriendlyByteBuf buf) {
        return new CastResultPacket(buf.readBoolean(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readInt());
    }

    public static void handle(CastResultPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (packet.success) {
                ClientSchoolData.noteServerCastSuccess(packet.schoolId, packet.spellId, packet.cooldownTicks);
                SihriyaUiSounds.success();
            } else {
                SihriyaNotifications.castBlocked(packet.schoolId, packet.reasonKey);
                ClientSchoolData.clearPendingCast();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
