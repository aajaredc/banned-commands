package com.bannedcommands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.stream.Collectors;

public final class BannedCommandsCommand {
    private BannedCommandsCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("bannedcommands")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    BannedCommandsConfig.load();
                                    ctx.getSource().sendSuccess(() -> Component.literal("Reloaded bannedcommands.json"), true);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("whereami")
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer p)) {
                                        ctx.getSource().sendFailure(Component.literal("Player-only command."));
                                        return 0;
                                    }
                                    ResourceLocation dim = p.serverLevel().dimension().location();
                                    ctx.getSource().sendSuccess(() -> Component.literal("Dimension ID: " + dim), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("list")
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer p)) {
                                        ctx.getSource().sendFailure(Component.literal("Player-only command."));
                                        return 0;
                                    }
                                    String dim = p.serverLevel().dimension().location().toString();
                                    Set<String> banned = BannedCommandsConfig.getBannedFor(dim);
                                    String msg = banned.isEmpty()
                                            ? "No banned commands for " + dim
                                            : "Banned for " + dim + ": " + banned.stream().sorted().collect(Collectors.joining(", "));
                                    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                                    return 1;
                                })
                        )
        );
    }
}