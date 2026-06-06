package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tong.sihriya.core.SpellHotbar;
import tong.sihriya.data.SpellRegistry;

import java.util.function.Supplier;

public class HotbarUpdatePacket {
    private final int slot;
    private final String spellId;

    public HotbarUpdatePacket(int slot, String spellId) {
        this.slot = slot;
        this.spellId = spellId;
    }

    public static void encode(HotbarUpdatePacket p, FriendlyByteBuf buf) {
        buf.writeByte(p.slot);
        buf.writeUtf(p.spellId, NetworkInputRules.MAX_SPELL_ID_LENGTH);
    }

    public static HotbarUpdatePacket decode(FriendlyByteBuf buf) {
        return new HotbarUpdatePacket(buf.readByte(), buf.readUtf(NetworkInputRules.MAX_SPELL_ID_LENGTH));
    }

    public static void handle(HotbarUpdatePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null && p.slot >= 0 && p.slot < 6) {
                if (p.spellId.isEmpty() || SpellRegistry.get(p.spellId) != null) {
                    SpellHotbar.setSpell(player, p.slot, p.spellId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
