package tong.sihriya.animation;

import net.minecraft.server.level.ServerPlayer;
import tong.sihriya.Sihriya;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class SihriyaAnimationPlayer {
    public enum SpellPhase {
        CHANT,
        CAST,
        CONTINUOUS
    }

    public static void play(ServerPlayer player, String spellId, SpellPhase phase) {
        ServerPlayerPatch patch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (patch == null) {
            Sihriya.LOGGER.warn("Cannot play animation for {} phase {}: no ServerPlayerPatch", spellId, phase);
            return;
        }

        String animName = resolveAnimation(spellId, phase);
        if (animName == null) {
            Sihriya.LOGGER.warn("No animation resolved for {} phase {}", spellId, phase);
            return;
        }

        var accessor = SihriyaAnimations.getByName(animName);
        if (accessor == null) {
            Sihriya.LOGGER.warn("Animation accessor '{}' not found (spell {} phase {})", animName, spellId, phase);
            return;
        }

        Sihriya.LOGGER.info(">> Playing animation {} for spell {} phase {}", animName, spellId, phase);
        if (accessor.get() instanceof StaticAnimation) {
            @SuppressWarnings("unchecked")
            var typedAccessor = (AnimationAccessor<? extends StaticAnimation>) accessor;
            patch.playAnimationSynchronized(typedAccessor, 0);
            Sihriya.LOGGER.info(">> playAnimationSynchronized called for {}", animName);
        } else {
            Sihriya.LOGGER.warn("Animation {} is not a StaticAnimation, got {}", animName, accessor.get().getClass());
        }
    }

    private static String resolveAnimation(String spellId, SpellPhase phase) {
        // D'abord chercher dans le SpellAnimationLoader (mapping JSON)
        String mapped = SpellAnimationLoader.getAnimation(spellId, phase);
        if (mapped != null) return mapped;

        // Fallback : basé sur le type de sort
        var spell = tong.sihriya.data.SpellRegistry.get(spellId);
        if (spell == null) return null;

        return switch (phase) {
            case CHANT -> "CHANTING_TWO_HAND_EXPLOSION";
            case CAST -> "CASTING_TWO_HAND_EXPLOSION";
            case CONTINUOUS -> "CONTINUOUS_TWO_HAND_FRONT";
        };
    }

    public static void playByName(ServerPlayer player, String animName) {
        ServerPlayerPatch patch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (patch == null) {
            Sihriya.LOGGER.warn("Cannot play animation {}: no ServerPlayerPatch", animName);
            return;
        }
        var accessor = SihriyaAnimations.getByName(animName);
        if (accessor == null) {
            Sihriya.LOGGER.warn("Animation '{}' not found", animName);
            return;
        }
        Sihriya.LOGGER.info(">> Playing animation by name {}", animName);
        if (accessor.get() instanceof StaticAnimation) {
            @SuppressWarnings("unchecked")
            var typedAccessor = (AnimationAccessor<? extends StaticAnimation>) accessor;
            patch.playAnimationSynchronized(typedAccessor, 0);
        } else {
            Sihriya.LOGGER.warn("Animation {} is not a StaticAnimation", animName);
        }
    }
}
