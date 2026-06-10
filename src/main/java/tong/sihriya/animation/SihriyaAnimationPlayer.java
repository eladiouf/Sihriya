package tong.sihriya.animation;

import net.minecraft.server.level.ServerPlayer;
import tong.sihriya.Sihriya;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class SihriyaAnimationPlayer {
    public enum SpellPhase { CHANT, CAST, CONTINUOUS }

    private static final float INSTANT_BLEND = 0.02f;

    public static void play(ServerPlayer player, String spellId, SpellPhase phase) {
        play(player, spellId, phase, INSTANT_BLEND);
    }

    public static void play(ServerPlayer player, String spellId, SpellPhase phase, float modifyTime) {
        ServerPlayerPatch patch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (patch == null) return;

        String animName = resolveAnimation(spellId, phase);
        if (animName == null) return;

        var accessor = SihriyaAnimations.getByName(animName);
        if (accessor == null) return;

        Object animObj = accessor.get();
        if (animObj instanceof StaticAnimation) {
            @SuppressWarnings("unchecked")
            var typedAccessor = (AnimationAccessor<? extends StaticAnimation>) accessor;
            try {
                patch.playAnimationSynchronized(typedAccessor, modifyTime);
                Sihriya.LOGGER.debug("playAnimationSynchronized {} for {} phase {}", animName, spellId, phase);
            } catch (Exception e) {
                Sihriya.LOGGER.error("playAnimationSynchronized failed: {}", e.toString());
            }
        }
    }

    private static String resolveAnimation(String spellId, SpellPhase phase) {
        String mapped = SpellAnimationLoader.getAnimation(spellId, phase);
        if (mapped != null) return mapped;

        String animKey = "ANIM_" + spellId.toUpperCase().replace('.', '_');
        if (SihriyaAnimations.getByName(animKey) != null) return animKey;

        var spell = tong.sihriya.data.SpellRegistry.get(spellId);
        if (spell == null) return null;

        String shortKey = spellId.substring(spellId.indexOf('.') + 1).toUpperCase();
        if (SihriyaAnimations.getByName(shortKey) != null) return shortKey;

        return switch (phase) {
            case CHANT -> "CHANTING_TWO_HAND_EXPLOSION";
            case CAST -> "CASTING_TWO_HAND_EXPLOSION";
            case CONTINUOUS -> "CONTINUOUS_TWO_HAND_FRONT";
        };
    }

    public static void playByName(ServerPlayer player, String animName) {
        playByName(player, animName, INSTANT_BLEND);
    }

    public static void playByName(ServerPlayer player, String animName, float modifyTime) {
        ServerPlayerPatch patch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (patch == null) return;
        var accessor = SihriyaAnimations.getByName(animName);
        if (accessor == null || !(accessor.get() instanceof StaticAnimation)) return;
        @SuppressWarnings("unchecked")
        var typedAccessor = (AnimationAccessor<? extends StaticAnimation>) accessor;
        patch.playAnimationSynchronized(typedAccessor, modifyTime);
    }
}
