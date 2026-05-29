package tong.sihriya.integration;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.core.SchoolProgressionProvider;
import tong.sihriya.data.SchoolRegistry;

/**
 * Ajoute des effets élémentaires aux attaques physiques (Epic Fight)
 * quand le joueur a une école active.
 * Ne remplace pas le système de sorts, les deux coexistent.
 */
@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class EpicFightEffects {
    private static final float EFFECT_CHANCE = 0.15f; // 15% chance on hit

    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        LivingEntity target = event.getEntity();

        player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).ifPresent(prog -> {
            String activeSchool = prog.getActiveSchool();
            if (activeSchool.isEmpty()) return;
            if (!prog.isSchoolUnlocked(activeSchool)) return;
            if (player.getRandom().nextFloat() > EFFECT_CHANCE) return;

            int schoolLevel = prog.getLevel(activeSchool);
            int duration = 40 + schoolLevel;

            switch (activeSchool) {
                case "fire" -> {
                    target.setRemainingFireTicks(duration * 2);
                    target.hurt(player.damageSources().indirectMagic(player, player),
                        schoolLevel * 0.1f);
                }
                case "water" -> {
                    target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
                }
                case "wind" -> {
                    var look = player.getLookAngle();
                    target.knockback(0.5f + schoolLevel * 0.01f, -look.x, -look.z);
                }
                case "earth" -> {
                    target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, duration, Math.min(2, schoolLevel / 20)));
                    target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, duration / 2, 2));
                }
                case "lightning" -> {
                    target.hurt(player.damageSources().lightningBolt(),
                        schoolLevel * 0.15f);
                    target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, duration / 2, 0));
                }
                case "ice" -> {
                    target.setTicksFrozen(duration);
                    target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, duration, 2));
                }
            }
        });
    }
}
