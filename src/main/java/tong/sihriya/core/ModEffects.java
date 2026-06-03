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
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true; // tick chaque frame pour maintenir mayfly
        }
    }
}
