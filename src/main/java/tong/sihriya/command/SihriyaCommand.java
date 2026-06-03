package tong.sihriya.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import tong.sihriya.Sihriya;
import tong.sihriya.animation.SihriyaAnimationPlayer;
import tong.sihriya.animation.SihriyaAnimationPlayer.SpellPhase;
import tong.sihriya.animation.SihriyaAnimations;
import tong.sihriya.animation.SpellAnimationLoader;
import tong.sihriya.core.SpellCastHandler;
import tong.sihriya.data.SpellRegistry;

public class SihriyaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sihriya")
            .then(Commands.literal("cast")
                .then(Commands.argument("spellId", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        String spellId = StringArgumentType.getString(ctx, "spellId");
                        boolean ok = SpellCastHandler.castSpell(player, spellId);
                        Sihriya.LOGGER.info("/sihriya cast {} result: {}", spellId, ok);
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
                        Sihriya.LOGGER.info("Animations for '{}': chant={}, cast={}, continuous={}",
                            spellId, chant, cast, cont);
                        ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                            "CHANT: " + chant + ", CAST: " + cast + ", CONT: " + cont), false);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("list_spells")
                .executes(ctx -> {
                    var spells = SpellRegistry.getAll();
                    var names = spells.stream().map(s -> s.id).toList();
                    Sihriya.LOGGER.info("Registered spells: {}", names);
                    ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
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
                            ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal(
                                "Animation not found: " + animName));
                            return 0;
                        }
                        SihriyaAnimationPlayer.playByName(player, animName);
                        ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                            "Playing animation: " + animName), false);
                        return 1;
                    })
                )
            )
        );
    }
}
