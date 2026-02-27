package com.bannedcommands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.stream.Collectors;

public final class BannedCommandsCommand {

    private BannedCommandsCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            register(dispatcher);
        });
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("bannedcommands")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    BannedCommandsConfig.load();
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Reloaded bannedcommands.json"),
                                            true
                                    );
                                    return 1;
                                })
                        )
                        .then(Commands.literal("status")
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                        ctx.getSource().sendFailure(Component.literal("Player-only command."));
                                        return 0;
                                    }

                                    String dim = player.serverLevel().dimension().location().toString();
                                    Set<String> banned = BannedCommandsConfig.getBannedFor(dim);
                                    String denyMessage = BannedCommandsConfig.getDenyMessage(dim);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Dimension: " + dim),
                                            false
                                    );

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Deny Message: " + denyMessage),
                                            false
                                    );

                                    if (banned.isEmpty()) {
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("Banned Commands: None"),
                                                false
                                        );
                                    } else {
                                        String list = banned.stream()
                                                .sorted()
                                                .collect(Collectors.joining(", "));
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("Banned Commands: " + list),
                                                false
                                        );
                                    }

                                    return 1;
                                })
                        )
        );
    }
}