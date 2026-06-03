package tong.sihriya.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.network.ManaSyncPacket;
import tong.sihriya.network.NetworkHandler;

@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class MeditationHandler {
    private static final float MANA_PER_TICK = 0.5f;
    private static final float PASSIVE_REGEN_RATE = 0.001f; // 0.1% du max mana par tick
    private static final int SLOWNESS_AMPLIFIER = 2;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;

        serverPlayer.getCapability(ManaProvider.MANA).ifPresent(mana -> {
            boolean wasLocked = mana.isLocked();
            long lockRemaining = mana.getLockRemainingMs();

            // Regen passive : 0.1% du max mana par tick
            float passiveRegen = mana.getMaxMana(serverPlayer) * PASSIVE_REGEN_RATE;
            mana.regenMana(passiveRegen, serverPlayer);

            // Méditation : sneak maintenu → regen rapide
            if (serverPlayer.isShiftKeyDown() && serverPlayer.getHealth() > 0) {
                mana.regenMana(MANA_PER_TICK, serverPlayer);
                if (serverPlayer.tickCount % 20 == 0) {
                    serverPlayer.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 40, SLOWNESS_AMPLIFIER, false, false, false));
                }
            }

            // Sync toutes les 20 ticks
            if (serverPlayer.tickCount % 20 == 0) {
                mana.clampMana(serverPlayer);
                NetworkHandler.sendToPlayer(
                    new ManaSyncPacket(mana.getMana(), mana.getMaxMana(serverPlayer),
                        mana.isLocked(), mana.getLockRemainingMs()),
                    serverPlayer);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerSleep(net.minecraftforge.event.entity.player.PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (event.wakeImmediately()) return;
        serverPlayer.getCapability(ManaProvider.MANA).ifPresent(mana -> {
            mana.setMana(mana.getMaxMana(serverPlayer));
            NetworkHandler.sendToPlayer(
                new ManaSyncPacket(mana.getMana(), mana.getMaxMana(serverPlayer), false, 0),
                serverPlayer);
        });
    }
}
