package tong.sihriya.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SchoolSyncPacketTest {

    @Test
    void decodeRejectsMalformedSchoolIdsInLevelSync() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf("fire", NetworkInputRules.MAX_SCHOOL_ID_LENGTH);
        buf.writeInt(1);
        buf.writeUtf("Fire", NetworkInputRules.MAX_SCHOOL_ID_LENGTH);
        buf.writeInt(12);
        buf.writeInt(0);
        buf.writeInt(0);

        assertThrows(IllegalArgumentException.class, () -> SchoolSyncPacket.decode(buf));
    }

    @Test
    void decodeRejectsMalformedSpellIds() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf("fire", NetworkInputRules.MAX_SCHOOL_ID_LENGTH);
        buf.writeInt(0);
        buf.writeInt(0);
        buf.writeInt(1);
        buf.writeUtf("fire/../spark", NetworkInputRules.MAX_SPELL_ID_LENGTH);

        assertThrows(IllegalArgumentException.class, () -> SchoolSyncPacket.decode(buf));
    }

    @Test
    void encodeRejectsOversizedSchoolSyncCollections() {
        Map<String, Integer> levels = new HashMap<>();
        for (int i = 0; i < NetworkInputRules.MAX_SYNC_ENTRIES + 1; i++) {
            levels.put("fire_" + i, i);
        }

        SchoolSyncPacket packet = new SchoolSyncPacket("fire", levels, Set.of(), Set.of());

        assertThrows(IllegalArgumentException.class,
            () -> SchoolSyncPacket.encode(packet, new FriendlyByteBuf(Unpooled.buffer())));
    }
}
