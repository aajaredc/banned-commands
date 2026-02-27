package com.bannedcommands.mixin;

import com.bannedcommands.BannedCommandsConfig;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(
            method = "performCommand(Lcom/mojang/brigadier/ParseResults;Ljava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bannedcommands_block(ParseResults<CommandSourceStack> parseResults, String input, CallbackInfo ci) {
        if (parseResults == null) return;

        CommandSourceStack source = parseResults.getContext().getSource();

        // OP bypass
        if (source.hasPermission(2)) return;

        if (!(source.getEntity() instanceof ServerPlayer player)) return;

        String levelId = player.serverLevel().dimension().location().toString();

        // Use Brigadier's reader string — includes the full command + args as typed
        String raw = parseResults.getReader().getString();
        String root = extractRoot(raw);

        if (!root.isEmpty() && BannedCommandsConfig.isBanned(levelId, root)) {
            String msg = BannedCommandsConfig.getDenyMessage(levelId);
            source.sendFailure(com.bannedcommands.ColorUtil.colored(msg));
            ci.cancel();
        }
    }

    private static String extractRoot(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.startsWith("/")) s = s.substring(1);
        // Collapse leading spaces again just in case
        s = s.trim();
        if (s.isEmpty()) return "";

        int space = s.indexOf(' ');
        String root = (space == -1) ? s : s.substring(0, space);
        return root.trim().toLowerCase(Locale.ROOT);
    }
}