package tong.sihriya.animation;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Armatures;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SihriyaAnimations {
    private static final Map<String, AnimationAccessor<?>> REGISTRY = new HashMap<>();

    @SubscribeEvent
    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        Sihriya.LOGGER.info("SihriyaAnimations.registerAnimations called, creating builder");
        event.newBuilder(Sihriya.MODID, SihriyaAnimations::build);
    }

    public static void build(AnimationManager.AnimationBuilder builder) {
        Sihriya.LOGGER.info("SihriyaAnimations.build() called");
        registerEfiscompat(builder);
        registerSkills(builder);
        registerCombat(builder);
        registerSpellAnimations(builder);
    }

    private static void register(AnimationManager.AnimationBuilder builder, String name, String path, boolean resetPose) {
        var accessor = builder.nextAccessor(path,
            a -> new StaticAnimation(resetPose, a, Armatures.BIPED));
        REGISTRY.put(name, accessor);
    }

    private static void registerEfiscompat(AnimationManager.AnimationBuilder builder) {
        register(builder, "CASTING_GOJO", "efiscompat/casting_gojo", true);
        register(builder, "CASTING_ONE_HAND_BELOW", "efiscompat/casting_one_hand_below", true);
        register(builder, "CASTING_ONE_HAND_BUFF", "efiscompat/casting_one_hand_buff", true);
        register(builder, "CASTING_ONE_HAND_INWARD", "efiscompat/casting_one_hand_inward", true);
        register(builder, "CASTING_ONE_HAND_STAFF_FRONT_LEFT", "efiscompat/casting_one_hand_staff_front_left", true);
        register(builder, "CASTING_ONE_HAND_STAFF_FRONT_RIGHT", "efiscompat/casting_one_hand_staff_front_right", true);
        register(builder, "CASTING_ONE_HAND_STAFF_TOP_LEFT", "efiscompat/casting_one_hand_staff_top_left", true);
        register(builder, "CASTING_ONE_HAND_STAFF_TOP_RIGHT", "efiscompat/casting_one_hand_staff_top_right", true);
        register(builder, "CASTING_ONE_HAND_TOP", "efiscompat/casting_one_hand_top", true);
        register(builder, "CASTING_TWO_HAND_ASCENSION", "efiscompat/casting_two_hand_ascension", true);
        register(builder, "CASTING_TWO_HAND_BACK", "efiscompat/casting_two_hand_back", true);
        register(builder, "CASTING_TWO_HAND_BELOW_RIGHT", "efiscompat/casting_two_hand_below_right", true);
        register(builder, "CASTING_TWO_HAND_BOW", "efiscompat/casting_two_hand_bow", true);
        register(builder, "CASTING_TWO_HAND_EXPLOSION", "efiscompat/casting_two_hand_explosion", true);
        register(builder, "CASTING_TWO_HAND_FLYING", "efiscompat/casting_two_hand_flying", true);
        register(builder, "CASTING_TWO_HAND_STAFF_TOP", "efiscompat/casting_two_hand_staff_top", true);
        register(builder, "CASTING_TWO_HAND_STOMP", "efiscompat/casting_two_hand_stomp", true);
        register(builder, "CASTING_TWO_HAND_TOP", "efiscompat/casting_two_hand_top", true);
        register(builder, "CHANTING_GOJO", "efiscompat/chanting_gojo", true);
        register(builder, "CHANTING_GOJO_POSE", "efiscompat/chanting_gojo_pose", true);
        register(builder, "CHANTING_ONE_HAND_FRONT", "efiscompat/chanting_one_hand_front", true);
        register(builder, "CHANTING_ONE_HAND_STAFF_LEFT", "efiscompat/chanting_one_hand_staff_left", true);
        register(builder, "CHANTING_ONE_HAND_STAFF_RIGHT", "efiscompat/chanting_one_hand_staff_right", true);
        register(builder, "CHANTING_ONE_HAND_STAFF_TOP_LEFT", "efiscompat/chanting_one_hand_staff_top_left", true);
        register(builder, "CHANTING_ONE_HAND_STOMP", "efiscompat/chanting_one_hand_stomp", true);
        register(builder, "CHANTING_ONE_HAND_TOP", "efiscompat/chanting_one_hand_top", true);
        register(builder, "CHANTING_TWO_HAND_ASCENSION", "efiscompat/chanting_two_hand_ascension", true);
        register(builder, "CHANTING_TWO_HAND_BACK", "efiscompat/chanting_two_hand_back", true);
        register(builder, "CHANTING_TWO_HAND_BOW", "efiscompat/chanting_two_hand_bow", true);
        register(builder, "CHANTING_TWO_HAND_EXPLOSION", "efiscompat/chanting_two_hand_explosion", true);
        register(builder, "CHANTING_TWO_HAND_FLYING", "efiscompat/chanting_two_hand_flying", true);
        register(builder, "CHANTING_TWO_HAND_STAFF_TOP", "efiscompat/chanting_two_hand_staff_top", true);
        register(builder, "CHANTING_TWO_HAND_STOMP", "efiscompat/chanting_two_hand_stomp", true);
        register(builder, "CHANTING_TWO_HAND_TOP", "efiscompat/chanting_two_hand_top", true);
        register(builder, "CONTINUOUS_ONE_HAND_STAFF_LEFT", "efiscompat/continuous_one_hand_staff_left", true);
        register(builder, "CONTINUOUS_ONE_HAND_STAFF_RIGHT", "efiscompat/continuous_one_hand_staff_right", true);
        register(builder, "CONTINUOUS_TWO_HAND_FRONT", "efiscompat/continuous_two_hand_front", true);
        register(builder, "CONTINUOUS_TWO_HAND_PUNCHING", "efiscompat/continuous_two_hand_punching", true);
    }

    private static void registerSkills(AnimationManager.AnimationBuilder builder) {
        register(builder, "BATTOJUTSU", "epicfight/biped/skill/battojutsu", false);
        register(builder, "BLADE_RUSH_COMBO1", "epicfight/biped/skill/blade_rush_combo1", false);
        register(builder, "BLADE_RUSH_COMBO2", "epicfight/biped/skill/blade_rush_combo2", false);
        register(builder, "BLADE_RUSH_COMBO3", "epicfight/biped/skill/blade_rush_combo3", false);
        register(builder, "BLADE_RUSH_EXECUTE", "epicfight/biped/skill/blade_rush_execute", false);
        register(builder, "DANCING_EDGE", "epicfight/biped/skill/dancing_edge", false);
        register(builder, "DEMOLITION_LEAP", "epicfight/biped/skill/demolition_leap", false);
        register(builder, "EVISCERATE_FIRST", "epicfight/biped/skill/eviscerate_first", false);
        register(builder, "GRASPING_SPIRE_SECOND", "epicfight/biped/skill/grasping_spire_second", false);
        register(builder, "HEARTPIERCER", "epicfight/biped/skill/heartpiercer", false);
        register(builder, "PHANTOM_ASCENT_FORWARD", "epicfight/biped/skill/phantom_ascent_forward", false);
        register(builder, "RELENTLESS_COMBO", "epicfight/biped/skill/relentless_combo", false);
        register(builder, "RUSHING_TEMPO1", "epicfight/biped/skill/rushing_tempo1", false);
        register(builder, "RUSHING_TEMPO2", "epicfight/biped/skill/rushing_tempo2", false);
        register(builder, "RUSHING_TEMPO3", "epicfight/biped/skill/rushing_tempo3", false);
        register(builder, "STEEL_WHIRLWIND", "epicfight/biped/skill/steel_whirlwind", false);
        register(builder, "SWEEPING_EDGE", "epicfight/biped/skill/sweeping_edge", false);
        register(builder, "THE_GUILLOTINE", "epicfight/biped/skill/the_guillotine", false);
        register(builder, "TSUNAMI", "epicfight/biped/skill/tsunami", false);
        register(builder, "WRATHFUL_LIGHTING", "epicfight/biped/skill/wrathful_lighting", false);
    }

    private static void registerCombat(AnimationManager.AnimationBuilder builder) {
        register(builder, "DAGGER_AUTO1", "epicfight/biped/combat/dagger_auto1", false);
        register(builder, "FIST_AUTO1", "epicfight/biped/combat/fist_auto1", false);
        register(builder, "GREATSWORD_AUTO1", "epicfight/biped/combat/greatsword_auto1", false);
        register(builder, "KNOCKDOWN", "epicfight/biped/combat/knockdown", false);
        register(builder, "SPEAR_TWOHAND_AUTO1", "epicfight/biped/combat/spear_twohand_auto1", false);
        register(builder, "SWORD_AUTO1", "epicfight/biped/combat/sword_auto1", false);
        register(builder, "SWORD_AUTO2", "epicfight/biped/combat/sword_auto2", false);
        register(builder, "TACHI_AUTO1", "epicfight/biped/combat/tachi_auto1", false);
    }

    private static void registerSpellAnimations(AnimationManager.AnimationBuilder builder) {
        register(builder, "FIRE_SPARK", "spells/fire_spark", true);
    }

    public static AnimationAccessor<?> getByName(String name) {
        if (name == null) return null;
        return REGISTRY.get(name);
    }
}
