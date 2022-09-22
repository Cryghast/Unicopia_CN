package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameRules;

class GravityCommand {

    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager
                .literal("gravity")
                .requires(s -> s.hasPermissionLevel(4));

        builder.then(CommandManager.literal("get")
                        .executes(context -> get(context.getSource(), context.getSource().getPlayer(), true))
               .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(context -> get(context.getSource(), EntityArgumentType.getPlayer(context, "target"), false))
                ));
        builder.then(CommandManager.literal("set")
               .then(CommandManager.argument("gravity", FloatArgumentType.floatArg(-99, 99))
                           .executes(context -> set(context.getSource(), context.getSource().getPlayer(), FloatArgumentType.getFloat(context, "gravity"), true))
               .then(CommandManager.argument("target", EntityArgumentType.player())
                           .executes(context -> set(context.getSource(), EntityArgumentType.getPlayer(context, "target"), FloatArgumentType.getFloat(context, "gravity"), false))
               )));

        dispatcher.register(builder);
    }

    static int get(ServerCommandSource source, PlayerEntity player, boolean isSelf) throws CommandSyntaxException {
        String translationKey = "commands.gravity.get";

        Pony iplayer = Pony.of(player);

        float gravity = iplayer.getPhysics().getGravityModifier();

        if (source.getPlayer() == player) {
            player.sendMessage(new TranslatableText(translationKey, gravity), false);
        } else {
            source.sendFeedback(new TranslatableText(translationKey + ".other", player.getName(), gravity), true);
        }

        return 0;
    }

    static int set(ServerCommandSource source, PlayerEntity player, float gravity, boolean isSelf) {
        String translationKey = "commands.gravity.set";

        Pony iplayer = Pony.of(player);

        iplayer.getPhysics().setBaseGravityModifier(gravity);
        iplayer.setDirty();

        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableText("commands.gamemode.success.self", gravity), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendMessage(new TranslatableText(translationKey, gravity), false);
            }

            source.sendFeedback(new TranslatableText(translationKey + ".other", player.getName(), gravity), true);
        }

        return 0;
    }
}
