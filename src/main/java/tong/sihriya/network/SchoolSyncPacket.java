package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class SchoolSyncPacket {
    private final String activeSchool;
    private final Map<String, Integer> schoolLevels;
    private final Set<String> unlockedSchools;
    private final Set<String> learnedSpells;

    public SchoolSyncPacket(String activeSchool, Map<String, Integer> schoolLevels,
                            Set<String> unlockedSchools, Set<String> learnedSpells) {
        this.activeSchool = activeSchool; this.schoolLevels = schoolLevels;
        this.unlockedSchools = unlockedSchools; this.learnedSpells = learnedSpells;
    }

    public static void encode(SchoolSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(NetworkInputRules.requireOptionalSchoolId(packet.activeSchool),
            NetworkInputRules.MAX_SCHOOL_ID_LENGTH);
        buf.writeInt(NetworkInputRules.requireSyncEntryCount(packet.schoolLevels.size()));
        for (var e : packet.schoolLevels.entrySet()) {
            buf.writeUtf(NetworkInputRules.requireSchoolId(e.getKey()), NetworkInputRules.MAX_SCHOOL_ID_LENGTH);
            buf.writeInt(e.getValue());
        }
        buf.writeInt(NetworkInputRules.requireSyncEntryCount(packet.unlockedSchools.size()));
        for (String s : packet.unlockedSchools) {
            buf.writeUtf(NetworkInputRules.requireSchoolId(s), NetworkInputRules.MAX_SCHOOL_ID_LENGTH);
        }
        buf.writeInt(NetworkInputRules.requireSyncEntryCount(packet.learnedSpells.size()));
        for (String s : packet.learnedSpells) {
            buf.writeUtf(NetworkInputRules.requireSpellId(s), NetworkInputRules.MAX_SPELL_ID_LENGTH);
        }
    }

    public static SchoolSyncPacket decode(FriendlyByteBuf buf) {
        String active = NetworkInputRules.requireOptionalSchoolId(
            buf.readUtf(NetworkInputRules.MAX_SCHOOL_ID_LENGTH));
        int lvlSize = NetworkInputRules.requireSyncEntryCount(buf.readInt());
        Map<String, Integer> levels = new HashMap<>();
        for (int i = 0; i < lvlSize; i++) {
            levels.put(NetworkInputRules.requireSchoolId(
                buf.readUtf(NetworkInputRules.MAX_SCHOOL_ID_LENGTH)), buf.readInt());
        }
        int uSize = NetworkInputRules.requireSyncEntryCount(buf.readInt());
        Set<String> unlocked = new HashSet<>();
        for (int i = 0; i < uSize; i++) {
            unlocked.add(NetworkInputRules.requireSchoolId(
                buf.readUtf(NetworkInputRules.MAX_SCHOOL_ID_LENGTH)));
        }
        int sSize = NetworkInputRules.requireSyncEntryCount(buf.readInt());
        Set<String> spells = new HashSet<>();
        for (int i = 0; i < sSize; i++) {
            spells.add(NetworkInputRules.requireSpellId(
                buf.readUtf(NetworkInputRules.MAX_SPELL_ID_LENGTH)));
        }
        return new SchoolSyncPacket(active, levels, unlocked, spells);
    }

    public static void handle(SchoolSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientPacketBridge.handleSchoolSync(packet.activeSchool, packet.schoolLevels,
                packet.unlockedSchools, packet.learnedSpells);
        });
        ctx.get().setPacketHandled(true);
    }
}
