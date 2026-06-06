package tong.sihriya.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import tong.sihriya.Sihriya;
import tong.sihriya.animation.SihriyaAnimationPlayer;
import tong.sihriya.animation.SihriyaAnimationPlayer.SpellPhase;
import tong.sihriya.animation.SihriyaAnimations;
import tong.sihriya.animation.SpellAnimationLoader;
import tong.sihriya.core.SchoolProgressionProvider;
import tong.sihriya.core.SpellCastHandler;
import tong.sihriya.core.TierUnlockHandler;
import tong.sihriya.data.SchoolRegistry;
import tong.sihriya.data.SpellRegistry;

public class SihriyaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sihriya")
            .requires(SihriyaCommand::canUseCommand)
            .then(Commands.literal("cast")
                .then(Commands.argument("spellId", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        String spellId = StringArgumentType.getString(ctx, "spellId");
                        if (SpellRegistry.get(spellId) == null) {
                            ctx.getSource().sendFailure(Component.literal("Unknown Sihriya spell: " + spellId));
                            return 0;
                        }
                        boolean ok = SpellCastHandler.castSpell(player, spellId);
                        Sihriya.LOGGER.info("/sihriya cast {} result: {}", spellId, ok);
                        if (!ok) {
                            ctx.getSource().sendFailure(Component.literal("Cannot cast Sihriya spell: " + spellId));
                        }
                        return ok ? 1 : 0;
                    })
                )
            )
            .then(Commands.literal("anim")
                .then(Commands.argument("spellId", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        String spellId = StringArgumentType.getString(ctx, "spellId");
                        String chant = SpellAnimationLoader.getAnimation(spellId, SpellPhase.CHANT);
                        String cast = SpellAnimationLoader.getAnimation(spellId, SpellPhase.CAST);
                        String cont = SpellAnimationLoader.getAnimation(spellId, SpellPhase.CONTINUOUS);
                        if (chant == null && cast == null && cont == null) {
                            ctx.getSource().sendFailure(Component.literal(
                                "No Sihriya animation mapping for spell: " + spellId));
                            return 0;
                        }
                        Sihriya.LOGGER.info("Animations for '{}': chant={}, cast={}, continuous={}",
                            spellId, chant, cast, cont);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "CHANT: " + chant + ", CAST: " + cast + ", CONT: " + cont), false);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("unlock_all")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    var opt = player.getCapability(SchoolProgressionProvider.SCHOOL_PROGRESSION).resolve();
                    if (opt.isEmpty()) {
                        ctx.getSource().sendFailure(Component.literal("Cannot access progression data"));
                        return 0;
                    }
                    var prog = opt.get();
                    for (var school : SchoolRegistry.getAll()) {
                        prog.unlockSchool(school.id);
                        prog.addXp(school.id, 10000000);
                        for (var spell : SpellRegistry.getBySchool(school.id)) {
                            prog.learnSpell(spell.id);
                        }
                    }
                    TierUnlockHandler.checkUnlocks(player, prog);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "Unlocked all schools at level 100 with all spells!"), false);
                    Sihriya.LOGGER.info("Unlocked all spells for {}", player.getName().getString());
                    return 1;
                })
            )
            .then(Commands.literal("list_spells")
                .executes(ctx -> {
                    var spells = SpellRegistry.getAll();
                    var names = spells.stream().map(s -> s.id).toList();
                    Sihriya.LOGGER.info("Registered spells: {}", names);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        spells.size() + " spells: " + String.join(", ", names)), false);
                    return 1;
                })
            )
            .then(Commands.literal("test_anim")
                .then(Commands.argument("animName", StringArgumentType.word())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        String animName = StringArgumentType.getString(ctx, "animName");
                        var accessor = SihriyaAnimations.getByName(animName);
                        if (accessor == null) {
                            ctx.getSource().sendFailure(Component.literal(
                                "Animation not found: " + animName));
                            return 0;
                        }
                        SihriyaAnimationPlayer.playByName(player, animName);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "Playing animation: " + animName), false);
                        return 1;
                    })
                )
            )
        );
    }

    private static boolean canUseCommand(CommandSourceStack source) {
        return source.hasPermission(2);
    }
}
