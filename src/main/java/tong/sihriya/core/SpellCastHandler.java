package tong.sihriya.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.*;
import tong.sihriya.integration.EpicFightIntegration;
import tong.sihriya.integration.STATModIntegration;
import tong.sihriya.network.NetworkHandler;
import tong.sihriya.network.NetworkInputRules;
import tong.sihriya.network.VFXTriggerPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gère l'exécution des sorts côté serveur.
 * Reçoit les demandes de cast, vérifie mana/cooldown/appris, exécute les effets.
 */
@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class SpellCastHandler {
    private static final SpellCooldownTracker COOLDOWNS = new SpellCooldownTracker();
    private static final java.util.Set<UUID> CASTING_PLAYERS = new java.util.HashSet<>();
    private static final java.util.Set<UUID> MOVEMENT_LOCKED = new java.util.HashSet<>();
    private static final java.util.Set<UUID> CANCELLED_CASTS = new java.util.HashSet<>();
    private static final java.util.Map<UUID, LivingEntity> LOCKED_TARGETS = new java.util.HashMap<>();
    private static final java.util.Map<UUID, Vec3> CAST_START_POSITIONS = new java.util.HashMap<>();

    public static boolean castSpell(ServerPlayer player, String spellId) {
        return castSpellDetailed(player, spellId).success();
    }

    public static CastResult castSpellDetailed(ServerPlayer player, String spellId) {
        SpellData spell = SpellRegistry.get(spellId);
        if (spell == null) return CastResult.fail("", "", "notification.sihriya.cast_no_spell");

        // Bloquer si un cast est déjà en cours pour ce joueur
        if (CASTING_PLAYERS.contains(player.getUUID())) {
            return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_no_spell");
        }

        // Vérifier si le sort est appris
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isEmpty()) return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_no_spell");
        var prog = progOpt.get();
        if (!prog.isSpellLearned(spellId)) return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_no_spell");

        // Vérifier si l'école est débloquée
        if (!prog.isSchoolUnlocked(spell.school)) return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_locked");

        // Vérifier le cooldown
        long now = System.currentTimeMillis();
        int remainingTicks = COOLDOWNS.remainingCooldownTicks(player.getUUID(), spellId, spell.cooldown, now);
        if (remainingTicks > 0) {
            return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_cooldown", remainingTicks);
        }

        // Vérifier le mana
        var manaOpt = player.getCapability(ManaProvider.MANA).resolve();
        if (manaOpt.isEmpty()) return CastResult.fail(spell.school, spell.id, "notification.sihriya.cast_no_mana");
        var mana = manaOpt.get();
        if (!mana.consumeMana(player, spell.manaCost)) {
            String reason = mana.isLocked(player) ? "notification.sihriya.cast_mana_locked" : "notification.sihriya.cast_no_mana";
            return CastResult.fail(spell.school, spell.id, reason);
        }

        // Enregistrer le cooldown dès maintenant pour éviter les doubles casts pendant le chant
        COOLDOWNS.recordCast(player.getUUID(), spellId, now);

        // Minimum 10 ticks de chant pour que l'animation soit bien visible
        final int CHANT_MIN_TICKS = 10;
        int chantTicks = spell.castTime > 0 ? Math.max(spell.castTime, CHANT_MIN_TICKS) : 0;

        if (chantTicks > 0 && !player.level().isClientSide) {
            // --- Phase CHANT : animation d'incantation, effets différés ---
            CASTING_PLAYERS.add(player.getUUID());

            // Lock mouvement pour les ULTIMATE (pas de déplacement, pas d'annulation)
            if (spell.type == SpellType.ULTIMATE) {
                MOVEMENT_LOCKED.add(player.getUUID());
                CAST_START_POSITIONS.put(player.getUUID(), player.position());
            }

            // Lock la cible au début du cast (ne change pas pendant le chant)
            if (spell.type != SpellType.PROJECTILE) {
                LOCKED_TARGETS.put(player.getUUID(), computeTargetEntity(player));
            }

            // Pour blazing_sun : prédire le point d'impact et envoyer le cercle magique immédiatement
            if ("fire.blazing_sun".equals(spellId)) {
                Vec3 eye = player.getEyePosition(1.0f);
                Vec3 look = player.getLookAngle().scale(120);
                var clip = player.level().clip(new ClipContext(
                    eye, eye.add(look), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                Vec3 hitPos = clip.getType() == HitResult.Type.MISS
                    ? eye.add(look) : clip.getLocation();
                NetworkHandler.sendToPlayer(new VFXTriggerPacket(spellId, spell.school,
                    player.getId(), hitPos.x, hitPos.y, hitPos.z), player);
            }

            // Blend ultra-rapide vers la pose de chant
            tong.sihriya.animation.SihriyaAnimationPlayer.play(player, spellId,
                tong.sihriya.animation.SihriyaAnimationPlayer.SpellPhase.CHANT);

            // Déclencher le CAST après chantTicks ticks
            final int POST_CAST_LOCK = 10;
            player.getServer().tell(new net.minecraft.server.TickTask(
                player.getServer().getTickCount() + chantTicks,
                () -> {
                    // Annuler si le joueur est mort, déconnecté, ou a bougé pendant le chant
                    if (player.isRemoved() || !player.isAlive() || CANCELLED_CASTS.contains(player.getUUID())) {
                        CANCELLED_CASTS.remove(player.getUUID());
                        CASTING_PLAYERS.remove(player.getUUID());
                        MOVEMENT_LOCKED.remove(player.getUUID());
                        LOCKED_TARGETS.remove(player.getUUID());
                        CAST_START_POSITIONS.remove(player.getUUID());
                        return;
                    }

                    // --- Phase CAST : animation de lancer ultra-rapide ---
                    tong.sihriya.animation.SihriyaAnimationPlayer.play(player, spellId,
                        tong.sihriya.animation.SihriyaAnimationPlayer.SpellPhase.CAST);

                    // Effets, XP, VFX (immédiats, l'animation de cast continue en parallèle)
                    executeEffects(player, spell);
                    LOCKED_TARGETS.remove(player.getUUID());
                    prog.addXp(spell.school, 5);
                    STATModIntegration.awardSpellXp(player, spell);
                    TierUnlockHandler.checkUnlocks(player, prog);
                    NetworkHandler.sendToPlayer(new VFXTriggerPacket(spellId, spell.school,
                        player.getId(), player.getX(), player.getY(), player.getZ()), player);

                    Sihriya.LOGGER.debug("Player {} cast spell {} (chant {}t → cast)",
                        player.getName().getString(), spellId, chantTicks);

                    // Débloquer après la durée de l'animation de cast
                    player.getServer().tell(new net.minecraft.server.TickTask(
                        player.getServer().getTickCount() + POST_CAST_LOCK,
                        () -> {
                            CASTING_PLAYERS.remove(player.getUUID());
                            MOVEMENT_LOCKED.remove(player.getUUID());
                            CAST_START_POSITIONS.remove(player.getUUID());
                        }
                    ));
                }
            ));
        } else {
            // --- Instant cast (castTime == 0) ---
            CASTING_PLAYERS.add(player.getUUID());
            if (spell.type == SpellType.ULTIMATE) {
                MOVEMENT_LOCKED.add(player.getUUID());
                CAST_START_POSITIONS.put(player.getUUID(), player.position());
            }
            if (spell.type != SpellType.PROJECTILE) {
                LOCKED_TARGETS.put(player.getUUID(), computeTargetEntity(player));
            }
            if (!player.level().isClientSide) {
                tong.sihriya.animation.SihriyaAnimationPlayer.play(player, spellId,
                    tong.sihriya.animation.SihriyaAnimationPlayer.SpellPhase.CAST);
            }
            executeEffects(player, spell);
            LOCKED_TARGETS.remove(player.getUUID());
            prog.addXp(spell.school, 5);
            STATModIntegration.awardSpellXp(player, spell);
            TierUnlockHandler.checkUnlocks(player, prog);
            NetworkHandler.sendToPlayer(new VFXTriggerPacket(spellId, spell.school,
                player.getId(), player.getX(), player.getY(), player.getZ()), player);

            Sihriya.LOGGER.debug("Player {} cast spell {} (instant)", player.getName().getString(), spellId);

            // Petit lock pour laisser l'animation de cast se jouer
            if (!player.level().isClientSide) {
                player.getServer().tell(new net.minecraft.server.TickTask(
                    player.getServer().getTickCount() + 10,
                    () -> {
                        CASTING_PLAYERS.remove(player.getUUID());
                        MOVEMENT_LOCKED.remove(player.getUUID());
                        CAST_START_POSITIONS.remove(player.getUUID());
                    }
                ));
            } else {
                CASTING_PLAYERS.remove(player.getUUID());
                MOVEMENT_LOCKED.remove(player.getUUID());
                CAST_START_POSITIONS.remove(player.getUUID());
            }
        }

        return CastResult.success(spell.school, spell.id, spell.cooldown);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        COOLDOWNS.clearPlayer(event.getEntity().getUUID());
        CASTING_PLAYERS.remove(event.getEntity().getUUID());
        MOVEMENT_LOCKED.remove(event.getEntity().getUUID());
        LOCKED_TARGETS.remove(event.getEntity().getUUID());
        CAST_START_POSITIONS.remove(event.getEntity().getUUID());
        CANCELLED_CASTS.remove(event.getEntity().getUUID());
    }

    // Lock mouvement pendant le cast des sorts ultimes + détection de mouvement → annulation
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            if (MOVEMENT_LOCKED.contains(event.player.getUUID())) {
                // Vérifier si le joueur a bougé → annuler le sort
                Vec3 start = CAST_START_POSITIONS.get(event.player.getUUID());
                if (start != null && event.player.position().distanceToSqr(start) > 0.01) {
                    CANCELLED_CASTS.add(event.player.getUUID());
                    MOVEMENT_LOCKED.remove(event.player.getUUID());
                    CAST_START_POSITIONS.remove(event.player.getUUID());
                    return;
                }
                // Bloquer tout mouvement
                event.player.xxa = 0;
                event.player.zza = 0;
                Vec3 motion = event.player.getDeltaMovement();
                event.player.setDeltaMovement(0, Math.min(0, motion.y), 0);
            }
        }
    }

    /** Cast le meilleur sort disponible pour une école (touché 1-6). */
    public static void castBySchool(ServerPlayer player, String schoolId) {
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isEmpty()) return;
        var prog = progOpt.get();

        if (!prog.isSchoolUnlocked(schoolId)) return;

        var spells = SpellSelectionRules.rankedCastCandidates(
            SpellRegistry.getAll(), prog.getLearnedSpells(), schoolId);

        for (var spell : spells) {
            if (castSpell(player, spell.id)) return;
        }
    }

    public static CastResult castBySchoolDetailed(ServerPlayer player, String schoolId) {
        if (!NetworkInputRules.isValidSchoolId(schoolId, id -> SchoolRegistry.get(id) != null)) {
            return CastResult.fail("", "", "notification.sihriya.cast_no_spell");
        }

        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isEmpty()) return CastResult.fail(schoolId, "", "notification.sihriya.cast_no_spell");
        var prog = progOpt.get();

        if (!prog.isSchoolUnlocked(schoolId)) return CastResult.fail(schoolId, "", "notification.sihriya.cast_locked");

        var spells = SpellSelectionRules.rankedCastCandidates(
            SpellRegistry.getAll(), prog.getLearnedSpells(), schoolId);

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

    /** Called by external systems (Iron's Spells, etc.) that already handled mana/cooldown. */
    public static void executeSpellEffects(ServerPlayer player, String spellId) {
        SpellData spell = SpellRegistry.get(spellId);
        if (spell == null) return;
        executeEffects(player, spell);
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isPresent()) {
            var prog = progOpt.get();
            prog.addXp(spell.school, 5);
            TierUnlockHandler.checkUnlocks(player, prog);
        }
        STATModIntegration.awardSpellXp(player, spell);
        NetworkHandler.sendToPlayer(new VFXTriggerPacket(spellId, spell.school,
            player.getId(), player.getX(), player.getY(), player.getZ()), player);
    }

    private static void executeEffects(ServerPlayer player, SpellData spell) {
        float schoolLevel = 0;
        var progOpt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
        if (progOpt.isPresent()) {
            schoolLevel = progOpt.get().getLevel(spell.school);
        }

        float statMultiplier = STATModIntegration.getDamageMultiplier(player, spell.school);

        float strongestEffectValue = 0;
        for (SpellEffect effect : spell.effects) {
            try {
                float value = (effect.baseValue + (schoolLevel * effect.scaling)) * statMultiplier;
                strongestEffectValue = Math.max(strongestEffectValue, value);
                int duration = effect.duration;

                var perkMod = getPerkModifiers(player, spell.school, effect.type);
                value *= perkMod.damageMult();
                duration = (int)(duration * perkMod.durationMult());
                int extraTargets = perkMod.extraTargets();

                switch (effect.type) {
                    case "damage" -> {
                        if (spell.type == SpellType.PROJECTILE) {
                            spawnProjectile(player, spell.school, value, spell.id);
                            continue;
                        }
                        applyDamageWithParticles(player, value, spell.school);
                    }
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
                    case "range_bonus" -> applyRangeBonus(player, value, duration);
                    case "orbit_damage" -> applyOrbitDamageAura(player, value, duration);
                    case "summon" -> applySummon(player, spell.school, (int) value, duration);
                    case "wall" -> applyWall(player, spell.school, (int) value, duration);
                    case "light" -> applyLight(player, duration);
                    case "water_walk" -> applyWaterWalk(player, duration);
                    case "water_breathing" -> applyWaterBreathing(player, duration);
                    case "mana_regen" -> applyManaRegen(player, value, duration);
                    case "fire_immunity" -> applyFireImmunity(player, duration);
                    case "physical_immunity" -> applyPhysicalImmunity(player, value, duration);
                    case "attack_speed" -> applyAttackSpeed(player, value, duration);
                    case "lava_immunity" -> applyLavaImmunity(player, duration);
                    case "ice_walk" -> applyIceWalk(player, duration);
                    case "fear_animals" -> applyFearAnimals(player, duration);
                    case "fear_immunity" -> applyFearImmunity(player, duration);
                    case "curse_immunity" -> applyCurseImmunity(player, duration);
                    case "heal_reduction" -> applyHealReduction(player, value, duration);
                    case "mana_drain" -> applyManaDrain(player, value);
                    case "resurrect" -> applyResurrect(player);
                    case "invulnerability" -> applyInvulnerability(player, value, duration);
                    case "banish" -> applyBanish(player);
                    case "invisibility" -> applyInvisibility(player, duration);
                    case "lightning_rod" -> applyLightningRod(player, duration);
                    case "barrier_undead" -> applyBarrierUndead(player, value, duration);
                    default -> Sihriya.LOGGER.debug("Unhandled effect type: {}", effect.type);
                }
            } catch (Throwable t) {
                Sihriya.LOGGER.debug("Effect '{}' suppressed: {}", effect.type, t.toString());
            }
        }
        try {
            applyPerkCastEffects(player, spell.school, strongestEffectValue);
        } catch (Throwable t) {
            Sihriya.LOGGER.debug("Perk effects suppressed: {}", t.toString());
        }

        spawnSpellVisuals(player, spell);
    }

    private static void spawnSpellVisuals(ServerPlayer player, SpellData spell) {
        var particleType = tong.sihriya.registry.SihriyaParticles.getForSchool(spell.school);
        var level = player.level();
        var target = getTargetEntity(player);
        var pos = target != null ? target.position() : player.position().add(player.getLookAngle().scale(3));
        net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket packet;

        switch (spell.type) {
            case ZONE -> {
                for (int i = 0; i < 15; i++) {
                    double angle = i * (Math.PI * 2 / 15);
                    packet = new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                        particleType, true,
                        pos.x + Math.cos(angle) * 2.5, pos.y + 0.3, pos.z + Math.sin(angle) * 2.5,
                        0f, 0.02f, 0f, 0.01f, 1);
                    broadcastPacket(level, packet);
                }
            }
            case BUFF -> {
                for (int i = 0; i < 12; i++) {
                    double angle = i * (Math.PI * 2 / 12);
                    packet = new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                        particleType, true,
                        player.getX() + Math.cos(angle) * 1.5, player.getY() + 0.8 + Math.sin(i * 0.5) * 0.5,
                        player.getZ() + Math.sin(angle) * 1.5,
                        0f, 0.01f, 0f, 0.01f, 1);
                    broadcastPacket(level, packet);
                }
            }
            case SUMMON -> {
                for (int i = 0; i < 20; i++) {
                    double angle = i * (Math.PI * 2 / 20);
                    packet = new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                        particleType, true,
                        player.getX() + Math.cos(angle) * 2, player.getY() + 0.2, player.getZ() + Math.sin(angle) * 2,
                        0f, 0.05f, 0f, 0.02f, 1);
                    broadcastPacket(level, packet);
                }
                for (int i = 0; i < 8; i++) {
                    packet = new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                        particleType, true,
                        player.getX() + (level.random.nextDouble() - 0.5) * 2, player.getY() + level.random.nextDouble() * 1.5,
                        player.getZ() + (level.random.nextDouble() - 0.5) * 2,
                        0f, 0.05f, 0f, 0.02f, 1);
                    broadcastPacket(level, packet);
                }
            }
            default -> {}
        }
    }

    private static void broadcastPacket(net.minecraft.world.level.Level level,
                                         net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket packet) {
        var server = level.getServer();
        if (server != null) {
            for (var p : server.getPlayerList().getPlayers()) {
                p.connection.send(packet);
            }
        }
    }

    // Effect implementations (simplified)
    private static void applyDamage(ServerPlayer player, float amount) {
        var target = getTargetEntity(player);
        if (target != null) {
            float finalAmount = amount;
            if (target instanceof net.minecraft.world.entity.player.Player targetPlayer) {
                float resistance = STATModIntegration.getMagicResistance(targetPlayer);
                finalAmount = amount * (1 - resistance);
            }
            target.hurt(player.damageSources().magic(), finalAmount);
        }
    }

    private static void applyDamageWithParticles(ServerPlayer player, float amount, String schoolId) {
        var target = getTargetEntity(player);
        if (target != null) {
            float finalAmount = amount;
            if (target instanceof net.minecraft.world.entity.player.Player targetPlayer) {
                float resistance = STATModIntegration.getMagicResistance(targetPlayer);
                finalAmount = amount * (1 - resistance);
            }
            target.hurt(player.damageSources().magic(), finalAmount);

            var particleType = tong.sihriya.registry.SihriyaParticles.getForSchool(schoolId);
            var packet = new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                particleType, true,
                target.getX(), target.getY() + 0.8, target.getZ(),
                0.5f, 0.3f, 0.5f, 0.02f, 12);
            broadcastPacket(player.level(), packet);
        }
    }

    private static void spawnProjectile(ServerPlayer player, String schoolId, float damage, String spellId) {
        var projectile = new tong.sihriya.projectile.SpellProjectile(
            player.level(), player, damage, schoolId, spellId);

        if ("fire.blazing_sun".equals(spellId)) {
            // Plane à +15Y, grossit, puis se lance
            projectile.setPos(player.getX(), player.getY() + 15.0, player.getZ());
            projectile.startCharge(560, 15.0, 19.0);
        } else {
            var look = player.getLookAngle();
            projectile.setPos(player.getX() + look.x * 1.5, player.getEyeY() - 0.3, player.getZ() + look.z * 1.5);
            projectile.shoot(look.x, look.y, look.z, 2.0f, 0.5f);
        }
        player.level().addFreshEntity(projectile);
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
        LivingEntity locked = LOCKED_TARGETS.get(player.getUUID());
        if (locked != null) return locked;
        return computeTargetEntity(player);
    }

    private static LivingEntity computeTargetEntity(ServerPlayer player) {
        double range = getDirectTargetRange(player);
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f);
        Vec3 end = start.add(look.scale(range));

        var level = player.level();
        var blockHit = level.clip(new ClipContext(
            start,
            end,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        var searchBox = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0);
        var entityHit = ProjectileUtil.getEntityHitResult(
            level,
            player,
            start,
            end,
            searchBox,
            SpellCastHandler::isValidDirectTarget
        );
        if (entityHit == null) return null;

        Entity entity = entityHit.getEntity();
        return entity instanceof LivingEntity living ? living : null;
    }

    private static boolean isValidDirectTarget(Entity entity) {
        return entity instanceof LivingEntity living
            && living.isAlive()
            && !living.isSpectator()
            && living.isPickable();
    }

    private static double getDirectTargetRange(ServerPlayer player) {
        var effect = player.getEffect(ModEffects.RANGE_EXTENSION.get());
        double multiplier = effect == null
            ? 1.0
            : SpellEffectRules.rangeMultiplierFromAmplifier(effect.getAmplifier());
        return 20.0 * multiplier;
    }

    private static void applyRangeBonus(ServerPlayer player, float bonus, int duration) {
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            ModEffects.RANGE_EXTENSION.get(),
            Math.max(1, duration),
            SpellEffectRules.rangeBonusAmplifier(bonus)
        ));
    }

    private static void applyOrbitDamageAura(ServerPlayer player, float damage, int duration) {
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            ModEffects.ORBIT_DAMAGE_AURA.get(),
            Math.max(20, duration),
            SpellEffectRules.orbitDamageAmplifier(damage)
        ));
    }

    private static void applySummon(ServerPlayer player, String school, int count, int duration) {
        int summonCount = TemporaryEffectRules.clampSummonCount(count);
        int summonDuration = TemporaryEffectRules.clampSummonDuration(duration);

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

        for (int i = 0; i < summonCount; i++) {
            double angle = TemporaryEffectRules.summonAngle(i, summonCount);
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
                    living.addTag(TemporaryEffectRules.TEMPORARY_SUMMON_TAG);
                    living.addTag(TemporaryEffectRules.SUMMON_CASTER_TAG_PREFIX + player.getUUID());
                    var target = getTargetEntity(player);
                    if (target != null && living instanceof net.minecraft.world.entity.Mob mob) {
                        mob.setTarget(target);
                        mob.setCanPickUpLoot(false);
                    }
                }
                player.level().addFreshEntity(entity);

                player.getServer().tell(new net.minecraft.server.TickTask(
                    player.getServer().getTickCount() + summonDuration,
                    () -> {
                        if (entity.isAlive()) {
                            entity.discard();
                        }
                    }
                ));
            }
        }
    }

    private static PerkModifierRules.Modifier getPerkModifiers(ServerPlayer player, String school, String effectType) {
        return PerkModifierRules.modifierFor(readPerkLevels(player), school, effectType);
    }

    private static PerkModifierRules.StatLevels readPerkLevels(ServerPlayer player) {
        return new PerkModifierRules.StatLevels(
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.FIRE_AFFINITY),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.WATER_AFFINITY),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.AIR_AFFINITY),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.EARTH_AFFINITY),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.ARCANE_POWER),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.MAGIC_RESISTANCE),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.CASTING_SPEED),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.ERUDITION)
        );
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

    private static void applyPerkCastEffects(ServerPlayer player, String school, float value) {
        for (var action : PerkCastRules.actionsFor(readCastPerkLevels(player), school)) {
            try {
                switch (action.type()) {
                    case FIRE_AOE -> nearbyEnemies(player, action.radius())
                        .forEach(e -> e.setRemainingFireTicks(action.duration()));
                    case FIRE_SPREAD -> applyPerkBurnSpread(player, school);
                    case WATER_PUSH -> nearbyEnemies(player, action.radius()).forEach(e -> {
                        Vec3 dir = e.position().subtract(player.position()).normalize();
                        e.push(dir.x * action.strength(), 0.3, dir.z * action.strength());
                        e.hurtMarked = true;
                    });
                    case TSUNAMI_SLOW -> nearbyEnemies(player, action.radius()).forEach(e ->
                        e.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                            action.duration(), action.amplifier())));
                    case WIND_PULL -> nearbyEnemies(player, action.radius()).forEach(e -> {
                        Vec3 dir = player.position().subtract(e.position()).normalize();
                        e.push(dir.x * action.strength(), 0.1, dir.z * action.strength());
                        e.hurtMarked = true;
                    });
                    case HURRICANE_LIFT -> nearbyEnemies(player, action.radius()).forEach(e -> {
                        Vec3 dir = player.position().subtract(e.position()).normalize();
                        e.push(dir.x * 0.25, action.strength(), dir.z * 0.25);
                        e.hurtMarked = true;
                    });
                    case EARTH_RESISTANCE -> safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                        action.duration(), action.amplifier()));
                    case EARTH_AOE_STUN -> nearbyEnemies(player, action.radius()).forEach(e ->
                        e.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                            action.duration(), action.amplifier())));
                    case ARCANE_AOE_DAMAGE -> nearbyEnemies(player, action.radius())
                        .forEach(e -> e.hurt(player.damageSources().magic(), value * action.strength()));
                    case MAGIC_RESISTANCE_ABSORB -> safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.ABSORPTION,
                        action.duration(), action.amplifier()));
                    case CASTING_SPEED_BOOST -> safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                        action.duration(), action.amplifier()));
                    case ERUDITION_REFUND, MANA_REFUND -> player.getCapability(ManaProvider.MANA).resolve()
                        .ifPresent(m -> m.regenMana(player, action.manaRefund()));
                }
            } catch (Throwable t) {
                Sihriya.LOGGER.debug("Perk effect suppressed: {}", t.toString());
            }
        }
    }

    private static void safeAddEffect(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.effect.MobEffectInstance effect) {
        try {
            entity.addEffect(effect);
        } catch (Throwable t) {
            Sihriya.LOGGER.debug("Suppressed potion effect for {}: {}", entity, t.toString());
        }
    }

    private static List<LivingEntity> nearbyEnemies(ServerPlayer player, double radius) {
        return player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
            e -> e != player && e.isAlive());
    }

    private static PerkCastRules.StatLevels readCastPerkLevels(ServerPlayer player) {
        return new PerkCastRules.StatLevels(
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.FIRE_AFFINITY),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.WATER_AFFINITY),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.AIR_AFFINITY),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.EARTH_AFFINITY),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.ARCANE_POWER),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.MAGIC_RESISTANCE),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.CASTING_SPEED),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.MANA_POOL),
            STATModIntegration.getStatLevel(player, tong.statmod.stats.StatType.ERUDITION)
        );
    }

    private static void applyWall(ServerPlayer player, String school, int durability, int duration) {
        int wallDuration = TemporaryEffectRules.clampWallDuration(duration);
        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();

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

        player.getServer().tell(new net.minecraft.server.TickTask(
            player.getServer().getTickCount() + wallDuration,
            () -> {
                for (var bp : placedBlocks) {
                    if (level.getBlockState(bp).getBlock() == blockState.getBlock()) {
                        level.setBlock(bp, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        ));
    }

    // ---- Missing effect implementations (Step 3) ----

    private static void applyLight(ServerPlayer player, int duration) {
        var level = player.level();
        var pos = player.blockPosition().above();
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.LIGHT.defaultBlockState(), 3);
            int cleanupDelay = Math.max(duration, 100);
            player.getServer().tell(new net.minecraft.server.TickTask(
                player.getServer().getTickCount() + cleanupDelay,
                () -> {
                    if (level.getBlockState(pos).getBlock() == net.minecraft.world.level.block.Blocks.LIGHT) {
                        level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            ));
        }
    }

    private static void applyWaterWalk(ServerPlayer player, int duration) {
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            ModEffects.WATER_WALK.get(), Math.max(1, duration), 0));
    }

    private static void applyWaterBreathing(ServerPlayer player, int duration) {
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.WATER_BREATHING, duration, 0));
    }

    private static void applyManaRegen(ServerPlayer player, float amount, int duration) {
        if (duration > 0) {
            int amp = Math.max(0, Math.round(amount) - 1);
            safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
                ModEffects.MANA_REGEN.get(), duration, amp));
        } else {
            player.getCapability(ManaProvider.MANA).ifPresent(m -> m.regenMana(player, amount));
        }
    }

    private static void applyFireImmunity(ServerPlayer player, int duration) {
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, duration, 0));
    }

    private static void applyPhysicalImmunity(ServerPlayer player, float amount, int duration) {
        int amp = Math.min(4, (int)(amount / 4));
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, duration, amp));
    }

    private static void applyAttackSpeed(ServerPlayer player, float amount, int duration) {
        int amp = Math.max(0, (int)(amount * 2) - 1);
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DIG_SPEED, duration, amp));
    }

    private static void applyLavaImmunity(ServerPlayer player, int duration) {
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, duration, 1));
    }

    private static void applyIceWalk(ServerPlayer player, int duration) {
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            ModEffects.ICE_WALK.get(), Math.max(1, duration), 0));
    }

    private static void applyFearAnimals(ServerPlayer player, int duration) {
        var nearby = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
            player.getBoundingBox().inflate(10),
            e -> e != player && e.isAlive()
                && e.getType().getCategory() == net.minecraft.world.entity.MobCategory.CREATURE);
        for (var animal : nearby) {
            animal.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, duration, 3));
            animal.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WEAKNESS, duration, 2));
            var away = animal.position().subtract(player.position()).normalize();
            animal.push(away.x * 0.5, 0.3, away.z * 0.5);
            animal.hurtMarked = true;
        }
    }

    private static void applyFearImmunity(ServerPlayer player, int duration) {
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            ModEffects.FEAR_IMMUNITY.get(), Math.max(1, duration), 0));
    }

    private static void applyCurseImmunity(ServerPlayer player, int duration) {
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            ModEffects.CURSE_IMMUNITY.get(), Math.max(1, duration), 0));
    }

    private static void applyHealReduction(ServerPlayer player, float amount, int duration) {
        var target = getTargetEntity(player);
        if (target != null) {
            int amp = Math.max(0, (int)(amount * 2) - 1);
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.WITHER, duration, amp));
        }
    }

    private static void applyManaDrain(ServerPlayer player, float amount) {
        var target = getTargetEntity(player);
        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.getCapability(ManaProvider.MANA).ifPresent(m -> {
                float drained = Math.min(amount, m.getMana(targetPlayer));
                m.consumeMana(targetPlayer, drained);
                player.getCapability(ManaProvider.MANA)
                    .ifPresent(pm -> pm.regenMana(player, drained * 0.5f));
            });
        } else if (target != null) {
            target.hurt(player.damageSources().magic(), amount * 0.3f);
            player.getCapability(ManaProvider.MANA)
                .ifPresent(pm -> pm.regenMana(player, amount * 0.1f));
        } else {
            player.getCapability(ManaProvider.MANA)
                .ifPresent(pm -> pm.regenMana(player, amount * 0.2f));
        }
    }

    private static void applyResurrect(ServerPlayer player) {
        for (var sp : player.getServer().getPlayerList().getPlayers()) {
            if (!sp.isAlive()) {
                sp.setHealth(20.0f);
                sp.clearFire();
                var spawnLevel = player.getServer().getLevel(sp.getRespawnDimension());
                if (spawnLevel == null) spawnLevel = (net.minecraft.server.level.ServerLevel) player.level();
                var respawn = sp.getRespawnPosition();
                if (respawn != null) {
                    sp.teleportTo(spawnLevel, respawn.getX(), respawn.getY(), respawn.getZ(),
                        sp.getYRot(), sp.getXRot());
                } else {
                    sp.teleportTo(spawnLevel, player.getX(), player.getY(), player.getZ(),
                        player.getYRot(), player.getXRot());
                }
            }
        }
    }

    private static void applyInvulnerability(ServerPlayer player, float amount, int duration) {
        int resAmp = Math.min(4, (int)(amount / 4));
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, duration, Math.max(4, resAmp)));
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.ABSORPTION, duration, Math.max(4, (int)(amount * 4))));
    }

    private static void applyBanish(ServerPlayer player) {
        var target = getTargetEntity(player);
        if (target != null) {
            target.kill();
            target.discard();
        }
    }

    private static void applyInvisibility(ServerPlayer player, int duration) {
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.INVISIBILITY, duration, 0));
    }

    private static void applyLightningRod(ServerPlayer player, int duration) {
        var level = player.level();
        var bolt = new net.minecraft.world.entity.LightningBolt(
            net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, level);
        bolt.setPos(player.getX(), player.getY(), player.getZ());
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
        safeAddEffect(player, new net.minecraft.world.effect.MobEffectInstance(
            ModEffects.LIGHTNING_ABSORPTION.get(), duration, 0));
    }

    private static void applyBarrierUndead(ServerPlayer player, float damage, int duration) {
        var nearby = player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
            player.getBoundingBox().inflate(5),
            e -> e.isAlive() && e.getMobType() == net.minecraft.world.entity.MobType.UNDEAD);
        for (var undead : nearby) {
            undead.hurt(player.damageSources().magic(), damage);
            undead.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                duration > 0 ? duration : 100, 4));
        }
    }
}
