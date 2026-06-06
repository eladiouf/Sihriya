package tong.sihriya.animation;

import net.minecraft.resources.ResourceLocation;
import tong.sihriya.Sihriya;

import java.util.*;

public class SpellAnimationManager {
    private static final Map<String, SpellAnimation> REGISTRY = new HashMap<>();

    public static void register(SpellAnimation anim) {
        REGISTRY.put(anim.spellId(), anim);
        Sihriya.LOGGER.debug("Registered animation for spell: {}", anim.spellId());
    }

    public static SpellAnimation get(String spellId) {
        return REGISTRY.get(spellId);
    }

    public static boolean hasAnimation(String spellId) {
        return REGISTRY.containsKey(spellId);
    }

    /** Charge les animations génériques utilisées comme fallback quand un sort n'a pas de mapping dédié. */
    public static void registerDefaults() {
        registerGeneric("PROJECTILE", "cast_projectile", 15, "flame");
        registerGeneric("ZONE", "cast_zone", 25, "spell");
        registerGeneric("BUFF", "cast_buff", 10, "happy_villager");
        registerGeneric("SUMMON", "cast_summon", 30, "witch");
        registerGeneric("ULTIMATE", "cast_ultimate", 60, "dragon_breath");
    }

    private static void registerGeneric(String type, String animPath, int duration, String particle) {
        ResourceLocation id = new ResourceLocation(Sihriya.MODID, animPath);
        REGISTRY.put(type, new SpellAnimation(id, type, duration, particle));
        Sihriya.LOGGER.debug("Fallback animation registered for type: {}", type);
    }
}
