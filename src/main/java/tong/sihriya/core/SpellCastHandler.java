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
        return castSpellDetailed(player, spellId).success();
    }

    public static CastResult castSpellDetailed(ServerPlayer player, String spellId) {
        SpellData spell = SpellRegistry.get(spellId);
        if (spell == null) return CastResult.fail("", "", "notification.sihriya.cast_no_spell");

        // Vérifier si le sort est appris
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isEmpty()) return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_no_spell");
        var prog = progOpt.get();
        if (!prog.isSpellLearned(spellId)) return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_no_spell");

        // Vérifier si l'école est débloquée
        if (!prog.isSchoolUnlocked(spell.school)) return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_locked");

        // Vérifier le cooldown
        var playerCooldowns = cooldowns.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        long now = System.currentTimeMillis();
        Long lastCast = playerCooldowns.get(spellId);
        if (lastCast != null && (now - lastCast) < spell.cooldown * 50L) {
            int remainingTicks = (int) Math.ceil((spell.cooldown * 50L - (now - lastCast)) / 50.0);
            return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_cooldown", remainingTicks);
        }

        // Vérifier le mana
        var manaOpt = player.getCapability(ManaProvider.MANA).resolve();
        if (manaOpt.isEmpty()) return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_no_mana");
        var mana = manaOpt.get();
        if (!mana.consumeMana(spell.manaCost)) {
            String reason = mana.isLocked() ? "notification.sihriya.cast_mana_locked" : "notification.sihriya.cast_no_mana";
            return CastResult.fail(spell.school, spell.id, reason);
        }

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
        return CastResult.success(spell.school, spell.id, spell.cooldown);
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

    public static CastResult castBySchoolDetailed(ServerPlayer player, String schoolId) {
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isEmpty()) return CastResult.fail(schoolId, "", "notification.sihriya.cast_no_spell");
        var prog = progOpt.get();

        if (!prog.isSchoolUnlocked(schoolId)) return CastResult.fail(schoolId, "", "notification.sihriya.cast_locked");

        var spells = prog.getLearnedSpells().stream()
            .map(SpellRegistry::get)
            .filter(Objects::nonNull)
            .filter(spell -> schoolId.equals(spell.school))
            .sorted((a, b) -> Integer.compare(b.tier, a.tier))
            .toList();

        if (spells.isEmpty()) return CastResult.fail(schoolId, "", "notification.sihriya.cast_no_spell");

        CastResult lastFailure = CastResult.fail(schoolId, spells.get(0).id, "notification.sihriya.cast_no_spell");
        for (var spell : spells) {
            CastResult result = castSpellDetailed(player, spell.id);
            if (result.success()) return result;
            lastFailure = result;
        }
        return lastFailure;
    }

    public record CastResult(boolean success, String schoolId, String spellId, String reasonKey, int cooldownTicks) {
        public static CastResult success(String schoolId, String spellId, int cooldownTicks) {
            return new CastResult(true, schoolId, spellId, "", cooldownTicks);
        }

        public static CastResult fail(String schoolId, String spellId, String reasonKey) {
            return fail(schoolId, spellId, reasonKey, 0);
        }

        public static CastResult fail(String schoolId, String spellId, String reasonKey, int cooldownTicks) {
            return new CastResult(false, schoolId, spellId, reasonKey, cooldownTicks);
        }
    }

    private static void executeEffects(ServerPlayer player, SpellData spell) {
        float schoolLevel = 0;
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isPresent()) {
            schoolLevel = progOpt.get().getLevel(spell.school);
        }

        float statMultiplier = STATModIntegration.getDamageMultiplier(player, spell.school);

        for (SpellEffect effect : spell.effects) {
            float value = (effect.baseValue + (schoolLevel * effect.scaling)) * statMultiplier;
            int duration = effect.duration;

            // Apply perk bonuses
            var perkMod = getPerkModifiers(player, spell.school, effect.type);
            value *= perkMod.damageMult();
            duration = (int)(duration * perkMod.durationMult());
            int extraTargets = perkMod.extraTargets();

            switch (effect.type) {
                case "damage" -> applyDamage(player, value);
                case "burn" -> { applyBurn(player, (int) value, duration); applyPerkBurnSpread(player, spell.school); }
                case "slow" -> applySlow(player, duration);
                case "knockback" -> applyKnockback(player, value);
                case "stun" -> applyStun(player, duration);
                case "freeze" -> applyFreeze(player, duration);
                case "chain" -> applyChain(player, value, Math.max(1, (int) value + extraTargets));
                case "heal" -> applyHeal(player, value);
                case "dash" -> applyDash(player, value);
                case "absorb_lightning" -> applyAbsorbLightning(player, duration);
                case "blindness" -> applyBlindness(player, duration);
                case "poison" -> applyPoison(player, (int) value, duration);
                case "pull" -> applyPull(player, value);
                case "fear" -> applyFear(player, duration);
                case "absorb" -> applyAbsorb(player, value, duration);
                case "dispel" -> applyDispel(player);
                case "speed" -> applySpeed(player, value, duration);
                case "thorns" -> applyThorns(player, value, duration);
                case "vulnerability" -> applyVulnerability(player, value, duration);
                case "damage_reduction" -> applyDamageReduction(player, value, duration);
                case "projectile_deflect" -> applyProjectileDeflect(player, value, duration);
                case "flight" -> applyFlight(player, duration);
                case "melee_bonus" -> applyMeleeBonus(player, value, duration);
                case "melee_fire_bonus" -> applyMeleeFireBonus(player, value, duration);
                case "range_bonus" -> {}
                case "orbit_damage" -> {}
                case "summon" -> applySummon(player, spell.school, (int) value, duration);
                case "wall" -> applyWall(player, spell.school, (int) value, duration);
                default -> Sihriya.LOGGER.debug("Unhandled effect type: {}", effect.type);
            }

            // Perk AoE effects after spell execution
            applyPerkAoEEffects(player, spell.school, value);
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

    private static void applySummon(ServerPlayer player, String school, int count, int duration) {
        if (count <= 0) count = 1;
        if (duration <= 0) duration = 600;

        net.minecraft.world.entity.EntityType<?> entityType = switch (school) {
            case "fire" -> net.minecraft.world.entity.EntityType.BLAZE;
            case "lava" -> net.minecraft.world.entity.EntityType.MAGMA_CUBE;
            case "earth" -> net.minecraft.world.entity.EntityType.IRON_GOLEM;
            case "necromancy" -> net.minecraft.world.entity.EntityType.ZOMBIE;
            case "lumamancy" -> net.minecraft.world.entity.EntityType.IRON_GOLEM;
            case "water" -> net.minecraft.world.entity.EntityType.SNOW_GOLEM;
            case "wind" -> net.minecraft.world.entity.EntityType.VEX;
            case "ice" -> net.minecraft.world.entity.EntityType.STRAY;
            default -> net.minecraft.world.entity.EntityType.ZOMBIE;
        };

        for (int i = 0; i < Math.min(count, 30); i++) {
            double angle = (2 * Math.PI * i) / count;
            double dx = Math.cos(angle) * 2.5;
            double dz = Math.sin(angle) * 2.5;
            double spawnX = player.getX() + dx;
            double spawnZ = player.getZ() + dz;
            double spawnY = player.getY();

            var entity = entityType.create(player.level());
            if (entity != null) {
                entity.moveTo(spawnX, spawnY, spawnZ,
                    player.getRandom().nextFloat() * 360f, 0f);
                if (entity instanceof LivingEntity living) {
                    living.addTag("sihriya_summon");
                    living.setPersistenceRequired();
                    // Set the summon's target to the caster's target
                    var target = getTargetEntity(player);
                    if (target != null && living instanceof net.minecraft.world.entity.Mob mob) {
                        mob.setTarget(target);
                    }
                }
                player.level().addFreshEntity(entity);

                // Schedule removal after duration
                int finalDuration = duration;
                player.getServer().tell(new net.minecraft.server.TickTask(
                    player.getServer().getTickCount() + finalDuration,
                    () -> {
                        if (entity.isAlive()) {
                            entity.discard();
                        }
                    }
                ));
            }
        }
    }

    private record PerkMod(float damageMult, float durationMult, int extraTargets) {
        static final PerkMod NONE = new PerkMod(1f, 1f, 0);
    }

    private static PerkMod getPerkModifiers(ServerPlayer player, String school, String effectType) {
        float dmg = 1f, dur = 1f;
        int extraTargets = 0;

        int fireLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.FIRE_AFFINITY);
        int waterLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.WATER_AFFINITY);
        int airLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.AIR_AFFINITY);
        int earthLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.EARTH_AFFINITY);
        int arcaneLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.ARCANE_POWER);
        int magicResLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.MAGIC_RESISTANCE);
        int castSpeedLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.CASTING_SPEED);
        int eruditionLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.ERUDITION);

        // ── AFFINITY PERKS ──
        switch (school) {
            case "fire", "lava" -> {
                if (fireLvl >= 20 && (effectType.equals("burn") || effectType.equals("damage"))) dmg = 1.25f;
            }
            case "water" -> {
                if (waterLvl >= 20 && (effectType.equals("slow") || effectType.equals("freeze"))) dur = 1.25f;
            }
            case "wind" -> {
                if (airLvl >= 20 && (effectType.equals("knockback") || effectType.equals("pull"))) dmg = 1.5f;
            }
            case "earth" -> {
                if (earthLvl >= 20 && effectType.equals("stun")) dur = 1.25f;
            }
        }

        // ── ARCANE_POWER perks ──
        if (arcaneLvl >= 20) dmg *= 1.25f;
        if (arcaneLvl >= 50 && effectType.equals("chain")) extraTargets = 2;

        // ── MAGIC_RESISTANCE perks ──
        // SHIELD_ADEQUAT (20): +15% damage reduction on absorb/damage_reduction spells
        if (magicResLvl >= 20 && (effectType.equals("absorb") || effectType.equals("damage_reduction"))) dur += 0.15f;
        // MUR_MAGIQUE (50): dispel also applies slowness to target
        if (magicResLvl >= 50 && effectType.equals("dispel")) extraTargets = 1;

        // ── CASTING_SPEED perks ──
        // RAPIDITE (20): dash distance +30%
        if (castSpeedLvl >= 20 && effectType.equals("dash")) dmg = 1.3f;
        // CANALISATION (50): heal +30%
        if (castSpeedLvl >= 50 && effectType.equals("heal")) dmg = 1.3f;

        // ── ERUDITION perks ──
        // SAVANT (20): +15% duration on buff spells (speed, flight, melee_bonus, thorns)
        if (eruditionLvl >= 20 && (effectType.equals("speed") || effectType.equals("flight")
            || effectType.equals("melee_bonus") || effectType.equals("thorns"))) dur = 1.15f;
        // MAITRE (50): +15% damage on all spells
        if (eruditionLvl >= 50) dmg *= 1.15f;

        return new PerkMod(dmg, dur, extraTargets);
    }

    private static void applyPerkBurnSpread(ServerPlayer player, String school) {
        int lvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.FIRE_AFFINITY);
        if (lvl < 80) return;
        var nearby = player.level().getEntitiesOfClass(LivingEntity.class,
            player.getBoundingBox().inflate(5), e -> e != player && e.isAlive());
        for (var e : nearby) {
            if (player.getRandom().nextFloat() < 0.3f) {
                e.setRemainingFireTicks(60);
            }
        }
    }

    private static void applyPerkAoEEffects(ServerPlayer player, String school, float value) {
        int fireLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.FIRE_AFFINITY);
        int waterLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.WATER_AFFINITY);
        int airLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.AIR_AFFINITY);
        int earthLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.EARTH_AFFINITY);
        int arcaneLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.ARCANE_POWER);
        int magicResLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.MAGIC_RESISTANCE);
        int castSpeedLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.CASTING_SPEED);
        int manaPoolLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.MANA_POOL);
        int eruditionLvl = STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.ERUDITION);

        // INFERNO (Fire 50): Fire AoE
        if (fireLvl >= 50 && school.equals("fire")) {
            var nearby = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3),
                e -> e != player && e.isAlive());
            for (var e : nearby) e.setRemainingFireTicks(80);
        }
        // PYROMANIA (Fire 80): Burn spread
        if (fireLvl >= 80 && school.equals("fire")) {
            applyPerkBurnSpread(player, school);
        }

        // TOURBILLON (Water 50): Push enemies
        if (waterLvl >= 50 && school.equals("water")) {
            var nearby = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4),
                e -> e != player && e.isAlive());
            for (var e : nearby) {
                Vec3 dir = e.position().subtract(player.position()).normalize();
                e.push(dir.x * 0.8, 0.3, dir.z * 0.8);
                e.hurtMarked = true;
            }
        }

        // TEMPETE (Wind 50): Pull enemies
        if (airLvl >= 50 && school.equals("wind")) {
            var nearby = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6),
                e -> e != player && e.isAlive());
            for (var e : nearby) {
                Vec3 dir = player.position().subtract(e.position()).normalize();
                e.push(dir.x * 0.6, 0.1, dir.z * 0.6);
                e.hurtMarked = true;
            }
        }

        // ROCHER (Earth 50): Absorption
        if (earthLvl >= 50 && school.equals("earth")) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 100, 0));
        }
        // CATACLYSME (Earth 80): AoE stun
        if (earthLvl >= 80 && school.equals("earth")) {
            var nearby = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(4),
                e -> e != player && e.isAlive());
            for (var e : nearby) {
                e.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
            }
        }

        // CATACLYSME_ARCANE (Arcane 80): AoE lightning
        if (arcaneLvl >= 80) {
            var nearby = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5),
                e -> e != player && e.isAlive());
            for (var e : nearby) {
                e.hurt(player.damageSources().magic(), value * 0.5f);
            }
        }

        // BOUCLIER_ARCANE (Magic Res 80): Self-absorb on any cast
        if (magicResLvl >= 80) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.ABSORPTION, 200, 0));
        }

        // PRESTO (Casting 80): Speed boost on any cast
        if (castSpeedLvl >= 80) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 80, 0));
        }

        // SAGE (Erudition 80): Small mana refund on cast
        if (eruditionLvl >= 80) {
            var manaOpt = player.getCapability(ManaProvider.MANA).resolve();
            manaOpt.ifPresent(m -> m.regenMana(5, player));
        }

        // RESERVOIR (Mana Pool 50): Max mana > 80 grants small regen each cast
        if (manaPoolLvl >= 50) {
            var manaOpt = player.getCapability(ManaProvider.MANA).resolve();
            manaOpt.ifPresent(m -> m.regenMana(3, player));
        }
    }

    private static void applyWall(ServerPlayer player, String school, int durability, int duration) {
        if (duration <= 0) duration = 1200; // 1 min default
        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();

        // Build a semicircular wall in front of the caster (5 blocks wide, 3 high)
        int width = 5;
        int height = 3;
        net.minecraft.world.level.block.state.BlockState blockState =
            switch (school) {
                case "fire", "lava" -> net.minecraft.world.level.block.Blocks.NETHERRACK.defaultBlockState();
                case "water", "ice" -> net.minecraft.world.level.block.Blocks.PACKED_ICE.defaultBlockState();
                case "earth" -> net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState();
                case "necromancy" -> net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState();
                case "lumamancy" -> net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK.defaultBlockState();
                default -> net.minecraft.world.level.block.Blocks.COBBLESTONE.defaultBlockState();
            };

        java.util.List<net.minecraft.core.BlockPos> placedBlocks = new java.util.ArrayList<>();
        var level = player.level();

        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                double offset = (w - width / 2.0) * 1.2;
                Vec3 pos = player.position()
                    .add(look.x * 2.5, h, look.z * 2.5)
                    .add(right.x * offset, 0, right.z * offset);
                net.minecraft.core.BlockPos bp =
                    new net.minecraft.core.BlockPos((int) Math.floor(pos.x),
                        (int) Math.floor(pos.y), (int) Math.floor(pos.z));

                if (level.getBlockState(bp).isAir() || level.getBlockState(bp).canBeReplaced()) {
                    level.setBlock(bp, blockState, 3);
                    placedBlocks.add(bp);
                }
            }
        }

        // Schedule removal after duration
        player.getServer().tell(new net.minecraft.server.TickTask(
            player.getServer().getTickCount() + duration,
            () -> {
                for (var bp : placedBlocks) {
                    if (level.getBlockState(bp).getBlock() == blockState.getBlock()) {
                        level.setBlock(bp, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        ));
    }

}
