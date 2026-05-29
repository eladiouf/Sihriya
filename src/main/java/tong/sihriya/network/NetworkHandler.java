package tong.sihriya.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import tong.sihriya.Sihriya;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Sihriya.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++, ManaSyncPacket.class,
            ManaSyncPacket::encode, ManaSyncPacket::decode, ManaSyncPacket::handle);
        CHANNEL.registerMessage(packetId++, SchoolSyncPacket.class,
            SchoolSyncPacket::encode, SchoolSyncPacket::decode, SchoolSyncPacket::handle);
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
