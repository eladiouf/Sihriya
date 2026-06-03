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
import tong.statmod.stats.StatType;

/**
 * Ajoute des effets élémentaires aux attaques physiques (Epic Fight)
 * quand le joueur a une école active.
 * Chance et intensité scale avec la stat STAT Mod correspondante.
 */
@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class EpicFightEffects {

    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        LivingEntity target = event.getEntity();

        player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).ifPresent(prog -> {
            String activeSchool = prog.getActiveSchool();
            if (activeSchool.isEmpty()) return;
            if (!prog.isSchoolUnlocked(activeSchool)) return;

            // Chance = niveau de la stat STAT Mod correspondante (max 100%)
            StatType stat = STATModIntegration.schoolToStat(activeSchool);
            int statLevel = stat != null ? STATModIntegration.getStatLevel(player, stat) : 0;
            float chance = Math.min(1.0f, statLevel * 0.01f); // 1% par niveau

            if (player.getRandom().nextFloat() > chance) return;

            int schoolLevel = prog.getLevel(activeSchool);
            int duration = 40 + schoolLevel;

            // Intensité scale avec la stat (multiplie les effets)
            float intensity = 1.0f + statLevel * 0.01f;

            switch (activeSchool) {
                case "fire" -> {
                    target.setRemainingFireTicks((int)(duration * 2 * intensity));
                    target.hurt(player.damageSources().indirectMagic(player, player),
                        schoolLevel * 0.1f * intensity);
                }
                case "water" -> {
                    target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, (int)(duration * intensity), 1));
                }
                case "wind" -> {
                    var look = player.getLookAngle();
                    target.knockback(0.5f + schoolLevel * 0.01f * intensity, -look.x, -look.z);
                }
                case "earth" -> {
                    target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, (int)(duration * intensity), Math.min(2, schoolLevel / 20)));
                    target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, (int)(duration / 2 * intensity), 2));
                }
                case "lightning" -> {
                    target.hurt(player.damageSources().lightningBolt(),
                        schoolLevel * 0.15f * intensity);
                    target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, (int)(duration / 2 * intensity), 0));
                }
                case "ice" -> {
                    target.setTicksFrozen((int)(duration * intensity));
                    target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, (int)(duration * intensity), 2));
                }
            }
        });
    }
}
