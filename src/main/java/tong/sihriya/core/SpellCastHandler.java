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
import tong.sihriya.integration.SihriyaAPI;

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

        // Exécuter les effets
        executeEffects(player, spell);

        // Cooldown
        playerCooldowns.put(spellId, now);

        // XP
        prog.addXp(spell.school, 5);

        // Check unlock conditions for advanced schools
        checkSchoolUnlocks(player, prog);

        Sihriya.LOGGER.debug("Player {} cast spell {}", player.getName().getString(), spellId);
        return true;
    }

    private static void executeEffects(ServerPlayer player, SpellData spell) {
        float schoolLevel = 0;
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isPresent()) {
            schoolLevel = progOpt.get().getLevel(spell.school);
        }

        // Bonus from STAT Mod
        float statBonus = SihriyaAPI.getScalingBonus(player, spell.school);

        for (SpellEffect effect : spell.effects) {
            float value = effect.baseValue + (schoolLevel * effect.scaling) * (1 + statBonus);

            switch (effect.type) {
                case "damage" -> applyDamage(player, value);
                case "burn" -> applyBurn(player, (int) value, effect.duration);
                case "slow" -> applySlow(player, effect.duration);
                case "knockback" -> applyKnockback(player, value);
                case "stun" -> applyStun(player, effect.duration);
                case "freeze" -> applyFreeze(player, effect.duration);
                case "chain" -> applyChain(player, value, effect.duration);
                case "heal" -> applyHeal(player, value);
            }
        }
    }

    // Effect implementations (simplified)
    private static void applyDamage(ServerPlayer player, float amount) {
        var target = getTargetEntity(player);
        if (target != null) target.hurt(player.damageSources().magic(), amount);
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

    private static LivingEntity getTargetEntity(ServerPlayer player) {
        var hitResult = player.pick(20.0, 1.0f, false);
        if (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
            var entityHit = ((net.minecraft.world.phys.EntityHitResult) hitResult).getEntity();
            if (entityHit instanceof LivingEntity living) return living;
        }
        return null;
    }

    private static void checkSchoolUnlocks(ServerPlayer player, SchoolProgression prog) {
        for (var school : tong.sihriya.data.SchoolRegistry.getAll()) {
            if (prog.isSchoolUnlocked(school.id)) continue;
            if (school.unlock == null) continue;

            boolean shouldUnlock = false;
            if ("or".equals(school.unlock.type)) {
                for (int i = 0; i < school.unlock.schoolIds.length; i++) {
                    if (prog.getLevel(school.unlock.schoolIds[i]) >= school.unlock.levels[i]) {
                        shouldUnlock = true;
                        break;
                    }
                }
            } else if ("level".equals(school.unlock.type)) {
                if (school.unlock.schoolIds.length > 0 &&
                    prog.getLevel(school.unlock.schoolIds[0]) >= school.unlock.levels[0]) {
                    shouldUnlock = true;
                }
            }

            if (shouldUnlock) {
                prog.unlockSchool(school.id);
                Sihriya.LOGGER.info("Player {} unlocked school: {}",
                    player.getName().getString(), school.id);
            }
        }
    }

    // Epic Fight compatibility: add elemental effects on hit
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        // Effects are applied through spell casting, not automatically on melee
    }
}
