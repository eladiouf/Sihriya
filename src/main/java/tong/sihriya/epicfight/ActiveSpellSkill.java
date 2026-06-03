package tong.sihriya.epicfight;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import tong.sihriya.core.SpellCastHandler;
import tong.sihriya.data.SpellRegistry;

/**
 * Skill Epic Fight qui lance un sort Sihriya.
 * Enregistré via SkillBuildEvent → ModRegistryWorker.build().
 */
public class ActiveSpellSkill extends Skill {
    private final String spellId;

    public ActiveSpellSkill(SkillBuilder<? extends Skill> builder, String spellId) {
        super(builder);
        this.spellId = spellId;
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        ServerPlayerPatch executor = container.getServerExecutor();
        if (executor == null) return false;
        ServerPlayer player = executor.getOriginal();

        var spell = SpellRegistry.get(spellId);
        if (spell == null) return false;

        var manaOpt = player.getCapability(tong.sihriya.core.ManaProvider.MANA).resolve();
        if (manaOpt.isEmpty()) return false;

        return !manaOpt.get().isLocked() && manaOpt.get().getMana() >= spell.manaCost;
    }

    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        ServerPlayerPatch executor = container.getServerExecutor();
        if (executor == null) return;
        ServerPlayer player = executor.getOriginal();

        SpellCastHandler.castSpell(player, spellId);
        container.activate();
    }

    public String getSpellId() {
        return spellId;
    }
}
