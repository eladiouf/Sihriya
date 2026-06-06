package tong.sihriya.epicfight;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import tong.sihriya.data.SpellRegistry;
import tong.sihriya.data.SpellRegistry.SpellData;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;

/**
 * Enregistre les sorts Sihriya comme skills Epic Fight via SkillBuildEvent.
 * Chaque sort devient un ActiveSpellSkill exécutable pendant le combat.
 */
@Mod.EventBusSubscriber(modid = Sihriya.MODID)
public class SkillMappingManager {
    private static final Map<String, ActiveSpellSkill> SPELL_TO_SKILL = new HashMap<>();
    private static final Map<String, String> SPELL_TO_ANIMATION = new HashMap<>();

    private static final Map<String, String> TYPE_ANIMATIONS = Map.of(
        "PROJECTILE", "biped/combat/magic_cast_projectile",
        "BUFF", "biped/combat/magic_cast_buff",
        "ZONE", "biped/combat/magic_cast_aoe",
        "ULTIMATE", "biped/combat/magic_cast_ultimate",
        "SUMMON", "biped/combat/magic_cast_summon"
    );

    @SubscribeEvent
    public static void onSkillBuild(SkillBuildEvent event) {
        var registry = event.createRegistryWorker(Sihriya.MODID);

        for (SpellData spell : SpellRegistry.getAll()) {
            String spellId = spell.id;
            String animType = TYPE_ANIMATIONS.getOrDefault(
                spell.type.name(), "biped/combat/magic_cast_projectile");

            SkillBuilder<ActiveSpellSkill> builder = new SkillBuilder<>();
            builder.setCategory(SkillCategories.BASIC_ATTACK);

            ActiveSpellSkill skill = registry.build(
                spellId,
                (SkillBuilder<ActiveSpellSkill> b) -> new ActiveSpellSkill(b, spellId),
                builder
            );

            SPELL_TO_SKILL.put(spellId, skill);
            SPELL_TO_ANIMATION.put(spellId, animType);
            Sihriya.LOGGER.debug("Registered Epic Fight skill: {} → {}", spellId, animType);
        }

        Sihriya.LOGGER.info("Registered {} Sihriya skills with Epic Fight", SPELL_TO_SKILL.size());
    }

    public static ActiveSpellSkill getSkillForSpell(String spellId) {
        return SPELL_TO_SKILL.get(spellId);
    }

    public static String getAnimationType(String spellId) {
        return SPELL_TO_ANIMATION.getOrDefault(spellId, "biped/combat/magic_cast_projectile");
    }
}
