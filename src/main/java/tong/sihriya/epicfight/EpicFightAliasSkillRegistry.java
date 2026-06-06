package tong.sihriya.epicfight;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tong.sihriya.Sihriya;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;

@Mod.EventBusSubscriber(modid = Sihriya.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EpicFightAliasSkillRegistry {
    private EpicFightAliasSkillRegistry() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSkillBuild(SkillBuildEvent event) {
        SkillBuildEvent.ModRegistryWorker worker = event.createRegistryWorker("efn");
        SkillBuilder builder = Skill.createBuilder();
        builder.setRegistryName(new ResourceLocation("efn", "example"));
        builder.setCategory(SkillCategories.WEAPON_INNATE);
        worker.build("example", EfnExampleAliasSkill::new, builder);
    }
}
