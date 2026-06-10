package tong.sihriya.core;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tong.sihriya.Sihriya;

/**
 * Effets custom Sihriya.
 * Registred via DeferredRegister sur le mod event bus.
 */
@Mod.EventBusSubscriber(modid = Sihriya.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Sihriya.MODID);

    /** Absorbe les dégâts de foudre et les convertit en mana */
    public static final RegistryObject<MobEffect> LIGHTNING_ABSORPTION =
        EFFECTS.register("lightning_absorption", () -> new LightningAbsorptionEffect());

    /** Renvoie des dégâts aux attaquants de mêlée */
    public static final RegistryObject<MobEffect> THORNS_AURA =
        EFFECTS.register("thorns_aura", () -> new ThornsAuraEffect());

    /** Dévie les projectiles entrants */
    public static final RegistryObject<MobEffect> PROJECTILE_SHIELD =
        EFFECTS.register("projectile_shield", () -> new ProjectileShieldEffect());

    /** Permet de voler (comme un créatif temporaire) */
    public static final RegistryObject<MobEffect> MAGIC_FLIGHT =
        EFFECTS.register("magic_flight", () -> new MagicFlightEffect());

    /** Augmente la portée des sorts ciblés. */
    public static final RegistryObject<MobEffect> RANGE_EXTENSION =
        EFFECTS.register("range_extension", () -> new RangeExtensionEffect());

    /** Inflige périodiquement des dégâts aux ennemis proches. */
    public static final RegistryObject<MobEffect> ORBIT_DAMAGE_AURA =
        EFFECTS.register("orbit_damage_aura", () -> new OrbitDamageAuraEffect());

    /** Restaure du mana périodiquement. */
    public static final RegistryObject<MobEffect> MANA_REGEN =
        EFFECTS.register("mana_regen", () -> new ManaRegenEffect());

    /** Permet de marcher sur l'eau en gelant la surface. */
    public static final RegistryObject<MobEffect> WATER_WALK =
        EFFECTS.register("water_walk", () -> new WaterWalkEffect());

    /** Gèle l'eau en glace compacte sous les pas. */
    public static final RegistryObject<MobEffect> ICE_WALK =
        EFFECTS.register("ice_walk", () -> new IceWalkEffect());

    /** Immunise contre la peur (faiblesse + lenteur). */
    public static final RegistryObject<MobEffect> FEAR_IMMUNITY =
        EFFECTS.register("fear_immunity", () -> new FearImmunityEffect());

    /** Immunise contre les malédictions (wither, dégâts instantanés). */
    public static final RegistryObject<MobEffect> CURSE_IMMUNITY =
        EFFECTS.register("curse_immunity", () -> new CurseImmunityEffect());

    // --- Implémentations ---

    private static class LightningAbsorptionEffect extends MobEffect {
        protected LightningAbsorptionEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x00AAFF);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return false; // pas de tick, géré par LivingHurtEvent
        }
    }

    private static class ThornsAuraEffect extends MobEffect {
        protected ThornsAuraEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x44FF44);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return false;
        }
    }

    private static class ProjectileShieldEffect extends MobEffect {
        protected ProjectileShieldEffect() {
            super(MobEffectCategory.BENEFICIAL, 0xFFFF00);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return false;
        }
    }

    private static class MagicFlightEffect extends MobEffect {
        protected MagicFlightEffect() {
            super(MobEffectCategory.BENEFICIAL, 0xCCAAFF);
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof Player player) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        }

        @Override
        public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
            super.removeAttributeModifiers(entity, attributeMap, amplifier);
            if (entity instanceof Player player) {
                if (SpellEffectRules.shouldRevokeMagicFlight(player.isCreative(), player.isSpectator())) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
            }
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true; // tick chaque frame pour maintenir mayfly
        }
    }

    private static class RangeExtensionEffect extends MobEffect {
        protected RangeExtensionEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x99DDFF);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return false;
        }
    }

    private static class OrbitDamageAuraEffect extends MobEffect {
        protected OrbitDamageAuraEffect() {
            super(MobEffectCategory.BENEFICIAL, 0xFFAA33);
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (!(entity instanceof Player player) || player.level().isClientSide) return;

            float damage = SpellEffectRules.orbitDamageFromAmplifier(amplifier);
            if (damage <= 0) return;

            var nearby = player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(3.5),
                target -> target != player && target.isAlive());
            for (var target : nearby) {
                target.hurt(player.damageSources().magic(), damage);
            }
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return duration % 20 == 0;
        }
    }

    private static class ManaRegenEffect extends MobEffect {
        protected ManaRegenEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x00FF88);
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (!(entity instanceof Player player) || player.level().isClientSide) return;
            float regenPerTick = (1 + amplifier) * 0.5f;
            player.getCapability(tong.sihriya.core.ManaProvider.MANA)
                .ifPresent(m -> m.regenMana(player, regenPerTick));
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return duration % 20 == 0;
        }
    }

    private static class WaterWalkEffect extends MobEffect {
        protected WaterWalkEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x4488FF);
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (!(entity instanceof Player player) || player.level().isClientSide) return;
            var pos = player.blockPosition();
            int radius = 2 + amplifier;
            var level = player.level();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    var bp = pos.offset(dx, -1, dz);
                    var state = level.getBlockState(bp);
                    if (state.getBlock() == net.minecraft.world.level.block.Blocks.WATER) {
                        level.setBlock(bp, net.minecraft.world.level.block.Blocks.FROSTED_ICE.defaultBlockState(), 3);
                    }
                }
            }
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return duration % 5 == 0;
        }
    }

    private static class IceWalkEffect extends MobEffect {
        protected IceWalkEffect() {
            super(MobEffectCategory.BENEFICIAL, 0xAAFFFF);
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (!(entity instanceof Player player) || player.level().isClientSide) return;
            var pos = player.blockPosition();
            int radius = 3 + amplifier;
            var level = player.level();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    var bp = pos.offset(dx, -1, dz);
                    var state = level.getBlockState(bp);
                    if (state.getBlock() == net.minecraft.world.level.block.Blocks.WATER
                        || state.getBlock() == net.minecraft.world.level.block.Blocks.FROSTED_ICE) {
                        level.setBlock(bp, net.minecraft.world.level.block.Blocks.ICE.defaultBlockState(), 3);
                    }
                }
            }
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return duration % 5 == 0;
        }
    }

    private static class FearImmunityEffect extends MobEffect {
        protected FearImmunityEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x8844AA);
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity.level().isClientSide) return;
            if (entity.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)) {
                entity.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
            }
            if (entity.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)) {
                entity.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
            }
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }
    }

    private static class CurseImmunityEffect extends MobEffect {
        protected CurseImmunityEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x6644AA);
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity.level().isClientSide) return;
            if (entity.hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) {
                entity.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
            }
            if (entity.hasEffect(net.minecraft.world.effect.MobEffects.HARM)) {
                entity.removeEffect(net.minecraft.world.effect.MobEffects.HARM);
            }
            if (entity.hasEffect(net.minecraft.world.effect.MobEffects.UNLUCK)) {
                entity.removeEffect(net.minecraft.world.effect.MobEffects.UNLUCK);
            }
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return duration % 20 == 0;
        }
    }
}
