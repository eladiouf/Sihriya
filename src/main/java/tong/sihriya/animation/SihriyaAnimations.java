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
import yesman.epicfight.api.animation.types.ActionAnimation;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SihriyaAnimations {
    private static final Map<String, AnimationAccessor<?>> REGISTRY = new HashMap<>();
    private static final Map<String, String> ANIMATION_PATHS = new HashMap<>();

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
        ANIMATION_PATHS.put(name, "animmodels/animations/" + path + ".json");
    }

    public static Map<String, String> getAllPaths() {
        return ANIMATION_PATHS;
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
        register(builder, "ANIM_CASTING_TWO_HAND_STOMP", "spells/casting_two_hand_stomp", true);
        register(builder, "ANIM_CHANTING_ONE_HAND_TOP", "spells/chanting_one_hand_top", true);
        register(builder, "ANIM_CHANTING_TWO_HAND_FLYING", "spells/chanting_two_hand_flying", true);
        register(builder, "ANIM_EARTH_EARTH_MOTHER_WRATH", "spells/earth_earth_mother_wrath", true);
        register(builder, "ANIM_EARTH_TITAN_AWAKENING", "spells/earth_titan_awakening", true);
        register(builder, "ANIM_FIRE_APOCALYPSE_IGNITED", "spells/fire_apocalypse_ignited", true);
        register(builder, "ANIM_FIRE_ASH_STORM", "spells/fire_ash_storm", true);
        register(builder, "ANIM_FIRE_BLAZING_ARMOR", "spells/fire_blazing_armor", true);
        register(builder, "ANIM_FIRE_BLAZING_SUN", "spells/fire_blazing_sun", true);
        register(builder, "ANIM_FIRE_BRANDS_MARK", "spells/fire_brands_mark", true);
        register(builder, "ANIM_FIRE_COMET", "spells/fire_comet", true);
        register(builder, "ANIM_FIRE_DANCING_EMBERS", "spells/fire_dancing_embers", true);
        register(builder, "ANIM_FIRE_FIRE_LANCE", "spells/fire_fire_lance", true);
        register(builder, "ANIM_FIRE_FIRE_NOVA", "spells/fire_fire_nova", true);
        register(builder, "ANIM_FIRE_FIRE_WHEEL", "spells/fire_fire_wheel", true);
        register(builder, "ANIM_FIRE_FIREBALL", "spells/fire_fireball", true);
        register(builder, "ANIM_FIRE_FLAME_DANCE", "spells/fire_flame_dance", true);
        register(builder, "ANIM_FIRE_FLYING_ASHES", "spells/fire_flying_ashes", true);
        register(builder, "ANIM_FIRE_FURNACE", "spells/fire_furnace", true);
        register(builder, "ANIM_FIRE_FURNACE_HEART", "spells/fire_furnace_heart", true);
        register(builder, "ANIM_FIRE_FUSION", "spells/fire_fusion", true);
        register(builder, "ANIM_FIRE_INFERNAL_PILLAR", "spells/fire_infernal_pillar", true);
        register(builder, "ANIM_FIRE_LIVING_TORCH", "spells/fire_living_torch", true);
        register(builder, "ANIM_FIRE_METEOR_SHOWER", "spells/fire_meteor_shower", true);
        register(builder, "ANIM_FIRE_MINOR_ERUPTION", "spells/fire_minor_eruption", true);
        register(builder, "ANIM_FIRE_PIERCING_METEOR", "spells/fire_piercing_meteor", true);
        register(builder, "ANIM_FIRE_PISTON_STRIKE", "spells/fire_piston_strike", true);
        register(builder, "ANIM_FIRE_SUMMONED_PHOENIX", "spells/fire_summoned_phoenix", true);
        register(builder, "ANIM_ICE_ETERNAL_WINTER", "spells/ice_eternal_winter", true);
        register(builder, "ANIM_ICE_ICE_AGE", "spells/ice_ice_age", true);
        register(builder, "ANIM_INFINITE_BRAZIER", "spells/infinite_brazier", true);
        register(builder, "ANIM_LAVA_CATACLYSMIC_ERUPTION", "spells/lava_cataclysmic_eruption", true);
        register(builder, "ANIM_LAVA_VOLCANIC_AWAKENING", "spells/lava_volcanic_awakening", true);
        register(builder, "ANIM_LIGHTNING_CELESTIAL_JUDGMENT", "spells/lightning_celestial_judgment", true);
        register(builder, "ANIM_LIGHTNING_RAINBOW_ARC", "spells/lightning_rainbow_arc", true);
        register(builder, "ANIM_LIGHTNING_STATIC_CHARGE", "spells/lightning_static_charge", true);
        register(builder, "ANIM_LIGHTNING_STORM_HEART", "spells/lightning_storm_heart", true);
        register(builder, "ANIM_LIGHTNING_WRATH_OF_ZEUS", "spells/lightning_wrath_of_zeus", true);
        register(builder, "ANIM_LUMAMANCY_CELESTIAL_PORTAL", "spells/lumamancy_celestial_portal", true);
        register(builder, "ANIM_LUMAMANCY_MINOR_ANGEL", "spells/lumamancy_minor_angel", true);
        register(builder, "ANIM_LUMAMANCY_MIRACLE_OF_AELIRA", "spells/lumamancy_miracle_of_aelira", true);
        register(builder, "ANIM_LUMAMANCY_PARADISE_FOUND", "spells/lumamancy_paradise_found", true);
        register(builder, "ANIM_LUMAMANCY_RESURRECTION", "spells/lumamancy_resurrection", true);
        register(builder, "ANIM_LUMAMANCY_SANCTUARY", "spells/lumamancy_sanctuary", true);
        register(builder, "ANIM_NECROMANCY_ARMY_OF_DEAD", "spells/necromancy_army_of_dead", true);
        register(builder, "ANIM_NECROMANCY_BLOOD_RITUAL", "spells/necromancy_blood_ritual", true);
        register(builder, "ANIM_NECROMANCY_CORRUPTED_RESURRECTION", "spells/necromancy_corrupted_resurrection", true);
        register(builder, "ANIM_NECROMANCY_HAND_OF_NOX", "spells/necromancy_hand_of_nox", true);
        register(builder, "ANIM_NECROMANCY_MACABRE_DANCE", "spells/necromancy_macabre_dance", true);
        register(builder, "ANIM_NECROMANCY_NECROPOLIS", "spells/necromancy_necropolis", true);
        register(builder, "ANIM_NECROMANCY_SOUL_SCYTHE", "spells/necromancy_soul_scythe", true);
        register(builder, "ANIM_NECROMANCY_SPECTRAL_KNIGHT", "spells/necromancy_spectral_knight", true);
        register(builder, "ANIM_STORM_BLADES", "spells/storm_blades", true);
        register(builder, "ANIM_WALL_OF_FLAMES", "spells/wall_of_flames", true);
        register(builder, "ANIM_WATER_ABYSSAL_WELL", "spells/water_abyssal_well", true);
        register(builder, "ANIM_WATER_AIR_BUBBLE", "spells/water_air_bubble", true);
        register(builder, "ANIM_WATER_AQUATIC_PURGE", "spells/water_aquatic_purge", true);
        register(builder, "ANIM_WATER_ASCENDING_CURRENT", "spells/water_ascending_current", true);
        register(builder, "ANIM_WATER_CRUSHING_WAVE", "spells/water_crushing_wave", true);
        register(builder, "ANIM_WATER_EYE_OF_DELUGE", "spells/water_eye_of_deluge", true);
        register(builder, "ANIM_WATER_FOG", "spells/water_fog", true);
        register(builder, "ANIM_WATER_HEALING_RAIN", "spells/water_healing_rain", true);
        register(builder, "ANIM_WATER_HOLY_WATER", "spells/water_holy_water", true);
        register(builder, "ANIM_WATER_LIQUID_BLADE", "spells/water_liquid_blade", true);
        register(builder, "ANIM_WATER_LIQUID_SHIELD", "spells/water_liquid_shield", true);
        register(builder, "ANIM_WATER_MAELSTROM", "spells/water_maelstrom", true);
        register(builder, "ANIM_WATER_PRESSURE_WAVE", "spells/water_pressure_wave", true);
        register(builder, "ANIM_WATER_PRIMEVAL_DELUGE", "spells/water_primeval_deluge", true);
        register(builder, "ANIM_WATER_SHARP_DROP", "spells/water_sharp_drop", true);
        register(builder, "ANIM_WATER_WALK_ON_WATER", "spells/water_walk_on_water", true);
        register(builder, "ANIM_WATER_WATER_JET", "spells/water_water_jet", true);
        register(builder, "ANIM_WATER_WATER_LACE", "spells/water_water_lace", true);
        register(builder, "ANIM_WATER_WATER_PRISON", "spells/water_water_prison", true);
        register(builder, "ANIM_WIND_AIR_SCISSORS", "spells/wind_air_scissors", true);
        register(builder, "ANIM_WIND_AIR_VACUUM", "spells/wind_air_vacuum", true);
        register(builder, "ANIM_WIND_ASCENSION", "spells/wind_ascension", true);
        register(builder, "ANIM_WIND_BRIEF_FLIGHT", "spells/wind_brief_flight", true);
        register(builder, "ANIM_WIND_ETHER_BREATH", "spells/wind_ether_breath", true);
        register(builder, "ANIM_WIND_EYE_OF_STORM", "spells/wind_eye_of_storm", true);
        register(builder, "ANIM_WIND_GUST", "spells/wind_gust", true);
        register(builder, "ANIM_WIND_HURRICANE", "spells/wind_hurricane", true);
        register(builder, "ANIM_WIND_IMPALING_WIND", "spells/wind_impaling_wind", true);
        register(builder, "ANIM_WIND_PROTECTIVE_BREATH", "spells/wind_protective_breath", true);
        register(builder, "ANIM_WIND_SILENT_GUST", "spells/wind_silent_gust", true);
        register(builder, "ANIM_WIND_WHISPER", "spells/wind_whisper", true);
        register(builder, "ANIM_WIND_WIND_CIRCLE", "spells/wind_wind_circle", true);
        register(builder, "ANIM_WIND_WIND_STEP", "spells/wind_wind_step", true);
        // register(builder, "ANIM_FIRE_BREATH_LOOP", "spells/breath_loop", true);
        // register(builder, "ANIM_FIRE_BREATH_PREPARATION", "spells/breath_preparation", true);
    }

    public static AnimationAccessor<?> getByName(String name) {
        if (name == null) return null;
        return REGISTRY.get(name);
    }
}
