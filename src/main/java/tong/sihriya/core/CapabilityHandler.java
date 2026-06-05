package tong.sihriya.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;

@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class CapabilityHandler {
    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(ManaManager.class);
        event.register(SchoolProgression.class);
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(
                ResourceLocation.fromNamespaceAndPath(Sihriya.MODID, "mana"),
                new ManaProvider());
            event.addCapability(
                ResourceLocation.fromNamespaceAndPath(Sihriya.MODID, "school_progression"),
                new SchoolProgressionProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        var original = event.getOriginal();
        original.reviveCaps();
        event.getEntity().getCapability(ManaProvider.MANA).ifPresent(newMana -> {
            original.getCapability(ManaProvider.MANA).ifPresent(oldMana ->
                newMana.deserializeNBT(oldMana.serializeNBT()));
        });
        event.getEntity().getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).ifPresent(newProg -> {
            original.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).ifPresent(oldProg ->
                newProg.deserializeNBT(oldProg.serializeNBT()));
        });
        original.invalidateCaps();
    }
}
