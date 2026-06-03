package tong.sihriya.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;

/**
 * Event handlers pour les effets custom Sihriya.
 * Gère : Lightning Absorption, Thorns Aura, Projectile Shield.
 */
@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class EffectHandlers {

    /**
     * Lightning Absorption : quand le joueur a l'effet et reçoit des dégâts de foudre,
     * les dégâts sont annulés et convertis en mana.
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Lightning Absorption
        if (player.hasEffect(ModEffects.LIGHTNING_ABSORPTION.get())) {
            String msg = event.getSource().getMsgId();
            if (msg.contains("lightning") || msg.contains("lightningBolt")) {
                float absorbed = event.getAmount();
                event.setCanceled(true);
                player.getCapability(ManaProvider.MANA).ifPresent(mana -> {
                    mana.regenMana(player, absorbed * 0.5f);
                });
            }
        }

        // Thorns Aura : renvoie des dégâts à l'attaquant de mêlée
        if (player.hasEffect(ModEffects.THORNS_AURA.get())) {
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                int amplifier = player.getEffect(ModEffects.THORNS_AURA.get()).getAmplifier();
                float thornsDamage = 2 + amplifier * 2;
                attacker.hurt(player.damageSources().thorns(player), thornsDamage);
            }
        }
    }

    /**
     * Projectile Shield : quand le joueur a l'effet, les projectiles sont déviés.
     */
    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.hasEffect(ModEffects.PROJECTILE_SHIELD.get())) return;

        // Si le projectile est un projectile de base, le dévier
        if (event.getDamageSource().getDirectEntity() != null &&
            event.getDamageSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile) {
            event.setShieldTakesDamage(false);
            event.setCanceled(true); // le bouclier absorbe sans durabilité
        }
    }
}
