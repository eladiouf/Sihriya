package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tong.sihriya.client.ClientManaData;

import java.util.function.Supplier;

public class ManaSyncPacket {
    private final float mana;
    private final float maxMana;
    private final boolean locked;
    private final long lockRemaining;

    public ManaSyncPacket(float mana, float maxMana, boolean locked, long lockRemaining) {
        this.mana = mana; this.maxMana = maxMana;
        this.locked = locked; this.lockRemaining = lockRemaining;
    }

    public static void encode(ManaSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.mana);
        buf.writeFloat(packet.maxMana);
        buf.writeBoolean(packet.locked);
        buf.writeLong(packet.lockRemaining);
    }

    public static ManaSyncPacket decode(FriendlyByteBuf buf) {
        return new ManaSyncPacket(buf.readFloat(), buf.readFloat(), buf.readBoolean(), buf.readLong());
    }

    public static void handle(ManaSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientManaData.mana = packet.mana;
            ClientManaData.maxMana = packet.maxMana;
            ClientManaData.locked = packet.locked;
            ClientManaData.lockRemaining = packet.lockRemaining;
        });
        ctx.get().setPacketHandled(true);
    }
}
