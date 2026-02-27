package com.bannedcommands;

import net.minecraft.network.chat.Component;

public final class ColorUtil {
    private ColorUtil() {}

    public static Component colored(String textWithAmpersands) {
        // simplest: replace & with § (Minecraft formatting char)
        String s = textWithAmpersands == null ? "" : textWithAmpersands.replace('&', '\u00A7');
        return Component.literal(s);
    }
}