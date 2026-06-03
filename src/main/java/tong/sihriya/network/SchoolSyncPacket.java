package tong.sihriya.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import tong.sihriya.client.ClientSchoolData;

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
        buf.writeUtf(packet.activeSchool);
        buf.writeInt(packet.schoolLevels.size());
        for (var e : packet.schoolLevels.entrySet()) {
            buf.writeUtf(e.getKey()); buf.writeInt(e.getValue());
        }
        buf.writeInt(packet.unlockedSchools.size());
        for (String s : packet.unlockedSchools) buf.writeUtf(s);
        buf.writeInt(packet.learnedSpells.size());
        for (String s : packet.learnedSpells) buf.writeUtf(s);
    }

    public static SchoolSyncPacket decode(FriendlyByteBuf buf) {
        String active = buf.readUtf();
        int lvlSize = buf.readInt();
        Map<String, Integer> levels = new HashMap<>();
        for (int i = 0; i < lvlSize; i++) levels.put(buf.readUtf(), buf.readInt());
        int uSize = buf.readInt();
        Set<String> unlocked = new HashSet<>();
        for (int i = 0; i < uSize; i++) unlocked.add(buf.readUtf());
        int sSize = buf.readInt();
        Set<String> spells = new HashSet<>();
        for (int i = 0; i < sSize; i++) spells.add(buf.readUtf());
        return new SchoolSyncPacket(active, levels, unlocked, spells);
    }

    public static void handle(SchoolSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientSchoolData.applySync(packet.activeSchool, packet.schoolLevels,
                packet.unlockedSchools, packet.learnedSpells);
        });
        ctx.get().setPacketHandled(true);
    }
}
