package tong.sihriya.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.*;
import tong.sihriya.integration.EpicFightIntegration;
import tong.sihriya.integration.STATModIntegration;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.network.SpellParticlePacket;

import java.util.*;

/**
 * Gère l'exécution des sorts côté serveur.
 * Reçoit les demandes de cast, vérifie mana/cooldown/appris, exécute les effets.
 */
@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class SpellCastHandler {
    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public static boolean castSpell(ServerPlayer player, String spellId) {
        SpellData spell = SpellRegistry.get(spellId);
        if (spell == null) return false;

        // Vérifier si le sort est appris
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isEmpty()) return false;
        var prog = progOpt.get();
        if (!prog.isSpellLearned(spellId)) return false;

        // Vérifier si l'école est débloquée
        if (!prog.isSchoolUnlocked(spell.school)) return false;

        // Vérifier le cooldown
        var playerCooldowns = cooldowns.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        long now = System.currentTimeMillis();
        Long lastCast = playerCooldowns.get(spellId);
        if (lastCast != null && (now - lastCast) < spell.cooldown * 50L) return false;

        // Vérifier le mana
        var manaOpt = player.getCapability(ManaProvider.MANA).resolve();
        if (manaOpt.isEmpty()) return false;
        var mana = manaOpt.get();
        if (!mana.consumeMana(spell.manaCost)) return false;

        // Jouer l'animation de chant (côté serveur seulement)
        if (!player.level().isClientSide) {
            tong.sihriya.animation.SihriyaAnimationPlayer.play(player, spellId,
                tong.sihriya.animation.SihriyaAnimationPlayer.SpellPhase.CHANT);
        }

        // Jouer l'animation Epic Fight (legacy)
        EpicFightIntegration.playSpellAnimation(player, spellId);

        // Exécuter les effets
        executeEffects(player, spell);

        // Après l'exécution des effets, jouer l'animation de cast
        if (!player.level().isClientSide) {
            tong.sihriya.animation.SihriyaAnimationPlayer.play(player, spellId,
                tong.sihriya.animation.SihriyaAnimationPlayer.SpellPhase.CAST);
        }

        // Cooldown
        playerCooldowns.put(spellId, now);

        // XP école Sihriya (progression locale)
        prog.addXp(spell.school, 5);

        // XP stats STAT Mod (MANA_POOL, CASTING_SPEED, stat d'affinité)
        STATModIntegration.awardSpellXp(player, spell);

        // Vérifier les paliers et déblocages
        TierUnlockHandler.checkUnlocks(player, prog);

        // Envoyer le packet de particules au client
        NetworkHandler.sendToPlayer(new SpellParticlePacket(spellId, spell.school), player);

        Sihriya.LOGGER.debug("Player {} cast spell {}", player.getName().getString(), spellId);
        return true;
    }

    /** Cast le meilleur sort disponible pour une école (touché 1-6). */
    public static void castBySchool(ServerPlayer player, String schoolId) {
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isEmpty()) return;
        var prog = progOpt.get();

        if (!prog.isSchoolUnlocked(schoolId)) return;

        // Trouver les sorts appris de cette école, triés par tier décroissant
        var spells = prog.getLearnedSpells().stream()
            .map(SpellRegistry::get)
            .filter(Objects::nonNull)
            .filter(spell -> schoolId.equals(spell.school))
            .sorted((a, b) -> Integer.compare(b.tier, a.tier))
            .toList();

        for (var spell : spells) {
            if (castSpell(player, spell.id)) return;
        }
    }

    private static void executeEffects(ServerPlayer player, SpellData spell) {
        float schoolLevel = 0;
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isPresent()) {
            schoolLevel = progOpt.get().getLevel(spell.school);
        }

        // Multiplicateur de dégâts basé sur les stats STAT Mod
        float statMultiplier = STATModIntegration.getDamageMultiplier(player, spell.school);

        for (SpellEffect effect : spell.effects) {
            float value = (effect.baseValue + (schoolLevel * effect.scaling)) * statMultiplier;

            switch (effect.type) {
                case "damage" -> applyDamage(player, value);
                case "burn" -> applyBurn(player, (int) value, effect.duration);
                case "slow" -> applySlow(player, effect.duration);
                case "knockback" -> applyKnockback(player, value);
                case "stun" -> applyStun(player, effect.duration);
                case "freeze" -> applyFreeze(player, effect.duration);
                case "chain" -> applyChain(player, value, effect.duration);
                case "heal" -> applyHeal(player, value);
                case "dash" -> applyDash(player, value);
                case "absorb_lightning" -> applyAbsorbLightning(player, effect.duration);
                case "blindness" -> applyBlindness(player, effect.duration);
                case "poison" -> applyPoison(player, (int) value, effect.duration);
                case "pull" -> applyPull(player, value);
                case "fear" -> applyFear(player, effect.duration);
                case "absorb" -> applyAbsorb(player, value, effect.duration);
                case "dispel" -> applyDispel(player);
                case "speed" -> applySpeed(player, value, effect.duration);
                case "thorns" -> applyThorns(player, value, effect.duration);
                case "vulnerability" -> applyVulnerability(player, value, effect.duration);
                case "damage_reduction" -> applyDamageReduction(player, value, effect.duration);
                case "projectile_deflect" -> applyProjectileDeflect(player, value, effect.duration);
                case "flight" -> applyFlight(player, effect.duration);
                case "melee_bonus" -> applyMeleeBonus(player, value, effect.duration);
                case "melee_fire_bonus" -> applyMeleeFireBonus(player, value, effect.duration);
                case "range_bonus" -> {} // passif, géré ailleurs
                case "orbit_damage" -> {} // passif, géré ailleurs
                case "summon" -> {} // TODO: invoquer des entités
                case "wall" -> {} // TODO: créer des blocs
                default -> Sihriya.LOGGER.debug("Unhandled effect type: {}", effect.type);
            }
        }
    }

    // Effect implementations (simplified)
    private static void applyDamage(ServerPlayer player, float amount) {
        var target = getTargetEntity(player);
        if (target != null) {
            // Appliquer la résistance magique de la cible
            float finalAmount = amount;
            if (target instanceof net.minecraft.world.entity.player.Player targetPlayer) {
                float resistance = STATModIntegration.getMagicResistance(targetPlayer);
                finalAmount = amount * (1 - resistance);
            }
            target.hurt(player.damageSources().magic(), finalAmount);
        }
    }

    private static void applyBurn(ServerPlayer player, int damage, int duration) {
        var target = getTargetEntity(player);
        if (target != null) {
            target.setRemainingFireTicks(duration);
            target.hurt(player.damageSources().inFire(), damage);
        }
    }

    private static void applySlow(ServerPlayer player, int duration) {
        var target = getTargetEntity(player);
        if (target != null) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
        }
    }

    private static void applyKnockback(ServerPlayer player, float strength) {
        var target = getTargetEntity(player);
        if (target != null) {
            Vec3 look = player.getLookAngle();
            target.knockback(strength, -look.x, -look.z);
        }
    }

    private static void applyStun(ServerPlayer player, int duration) {
        var target = getTargetEntity(player);
        if (target != null) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS, duration, 4));
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, duration, 5));
        }
    }

    private static void applyFreeze(ServerPlayer player, int duration) {
        var target = getTargetEntity(player);
        if (target != null) {
            target.setTicksFrozen(duration);
        }
    }

    private static void applyChain(ServerPlayer player, float damage, int maxTargets) {
        var targets = player.level().getEntitiesOfClass(
            LivingEntity.class, player.getBoundingBox().inflate(8),
            e -> e != player && e.isAlive());
        int count = 0;
        for (LivingEntity t : targets) {
            if (count >= maxTargets) break;
            t.hurt(player.damageSources().magic(), damage);
            t.setRemainingFireTicks(40);
            count++;
        }
    }

    private static void applyHeal(ServerPlayer player, float amount) {
        player.heal(amount);
    }

    private static void applyDash(ServerPlayer player, float distance) {
        Vec3 look = player.getLookAngle();
        player.push(look.x * distance * 0.5, 0.15, look.z * distance * 0.5);
        player.hurtMarked = true;
    }

    private static void applyAbsorbLightning(ServerPlayer player, int duration) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            tong.sihriya.core.ModEffects.LIGHTNING_ABSORPTION.get(), duration, 0));
    }

    private static void applyBlindness(ServerPlayer player, int duration) {
        var target = getTargetEntity(player);
        if (target != null) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.BLINDNESS, duration, 0));
        }
    }

    private static void applyPoison(ServerPlayer player, int damage, int duration) {
        var target = getTargetEntity(player);
        if (target != null) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.POISON, duration, 1));
        }
    }

    private static void applyPull(ServerPlayer player, float strength) {
        var target = getTargetEntity(player);
        if (target != null) {
            Vec3 direction = player.position().subtract(target.position()).normalize();
            target.push(direction.x * strength * 0.3, 0.1, direction.z * strength * 0.3);
            target.hurtMarked = true;
        }
    }

    private static void applyFear(ServerPlayer player, int duration) {
        var target = getTargetEntity(player);
        if (target != null) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, duration, 3));
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS, duration, 2));
        }
    }

    private static void applyAbsorb(ServerPlayer player, float amount, int duration) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.ABSORPTION, duration, (int)(amount / 4)));
    }

    private static void applyDispel(ServerPlayer player) {
        var target = getTargetEntity(player);
        if (target != null) {
            // Supprimer les effets positifs de la cible
            var effects = new ArrayList<>(target.getActiveEffects());
            for (var effect : effects) {
                if (effect.getEffect().isBeneficial()) {
                    target.removeEffect(effect.getEffect());
                }
            }
        }
    }

    private static void applySpeed(ServerPlayer player, float multiplier, int duration) {
        int amplifier = (int)(multiplier * 2);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, duration, Math.max(0, amplifier)));
    }

    private static void applyThorns(ServerPlayer player, float damage, int duration) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            tong.sihriya.core.ModEffects.THORNS_AURA.get(), duration, (int)(damage / 4)));
    }

    private static void applyVulnerability(ServerPlayer player, float multiplier, int duration) {
        var target = getTargetEntity(player);
        if (target != null) {
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS, duration, (int)(multiplier * 4)));
        }
    }

    private static void applyDamageReduction(ServerPlayer player, float amount, int duration) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, duration, (int)(amount * 4)));
    }

    private static void applyProjectileDeflect(ServerPlayer player, float chance, int duration) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            tong.sihriya.core.ModEffects.PROJECTILE_SHIELD.get(), duration, 0));
    }

    private static void applyFlight(ServerPlayer player, int duration) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            tong.sihriya.core.ModEffects.MAGIC_FLIGHT.get(), duration, 0));
    }

    private static void applyMeleeBonus(ServerPlayer player, float amount, int duration) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, duration, (int)(amount / 4)));
    }

    private static void applyMeleeFireBonus(ServerPlayer player, float amount, int duration) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, duration, (int)(amount / 4)));
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, duration, 0));
    }

    private static LivingEntity getTargetEntity(ServerPlayer player) {
        var hitResult = player.pick(20.0, 1.0f, false);
        if (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
            var entityHit = ((net.minecraft.world.phys.EntityHitResult) hitResult).getEntity();
            if (entityHit instanceof LivingEntity living) return living;
        }
        return null;
    }

}
