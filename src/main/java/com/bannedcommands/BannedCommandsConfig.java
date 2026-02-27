package com.bannedcommands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class BannedCommandsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "bannedcommands.json";

    private static final Type TYPE = new TypeToken<Map<String, DimensionEntry>>() {}.getType();

    private static volatile Map<String, DimensionEntry> entries = new HashMap<>();

    private BannedCommandsConfig() {}

    public static final class DimensionEntry {
        public String denyMessage;
        public List<String> commands;

        // convenience normalization cache (not serialized)
        private transient Set<String> commandsSet;
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);

        if (Files.notExists(path)) {
            writeDefault(path);
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            Map<String, DimensionEntry> raw = GSON.fromJson(reader, TYPE);
            if (raw == null) raw = new HashMap<>();

            // normalize
            for (var e : raw.entrySet()) {
                DimensionEntry entry = e.getValue();
                if (entry == null) continue;

                Set<String> set = new HashSet<>();
                if (entry.commands != null) {
                    for (String c : entry.commands) {
                        if (c == null) continue;
                        String n = normalize(c);
                        if (!n.isEmpty()) set.add(n);
                    }
                }
                entry.commandsSet = set;

                if (entry.denyMessage != null) {
                    entry.denyMessage = entry.denyMessage.trim();
                }
            }

            entries = raw;
            System.out.println("[BANNED COMMANDS] Loaded entries: " + entries.keySet());
        } catch (Exception ex) {
            System.err.println("[BANNED COMMANDS] Failed to load config: " + ex.getMessage());
            entries = new HashMap<>();
        }
    }

    public static boolean isBanned(String dimensionId, String commandRoot) {
        DimensionEntry entry = getEntry(dimensionId);
        if (entry == null || entry.commandsSet == null || entry.commandsSet.isEmpty()) return false;

        String root = normalize(commandRoot);

        if (entry.commandsSet.contains(root)) return true;

        // allow banning "warp" to match "plugin:warp"
        int colon = root.indexOf(':');
        if (colon >= 0 && colon + 1 < root.length()) {
            return entry.commandsSet.contains(root.substring(colon + 1));
        }

        return false;
    }

    public static String getDenyMessage(String dimensionId) {
        DimensionEntry entry = getEntry(dimensionId);
        if (entry != null && entry.denyMessage != null && !entry.denyMessage.isBlank()) {
            return entry.denyMessage;
        }
        DimensionEntry def = entries.get("default");
        if (def != null && def.denyMessage != null && !def.denyMessage.isBlank()) {
            return def.denyMessage;
        }
        return "&cYou cannot use that command here.";
    }

    private static DimensionEntry getEntry(String dimensionId) {
        DimensionEntry specific = entries.get(dimensionId);
        if (specific != null) return specific;
        return entries.get("default");
    }

    private static String normalize(String s) {
        s = s.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith("/")) s = s.substring(1);
        return s;
    }

    private static void writeDefault(Path path) {
        try {
            Files.createDirectories(path.getParent());

            Map<String, DimensionEntry> example = new LinkedHashMap<>();

            DimensionEntry def = new DimensionEntry();
            def.denyMessage = "&cYou cannot use that command here.";
            def.commands = List.of();
            example.put("default", def);

            DimensionEntry overworld = new DimensionEntry();
            overworld.denyMessage = "&cNo teleports in the overworld.";
            overworld.commands = List.of("warp", "rtp", "spawn");
            example.put("minecraft:overworld", overworld);

            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                GSON.toJson(example, writer);
            }
        } catch (IOException ignored) {}
    }

    public static Set<String> getBannedFor(String dimensionId) {
        DimensionEntry entry = entries.get(dimensionId);
        if (entry != null && entry.commandsSet != null) {
            return Set.copyOf(entry.commandsSet);
        }

        DimensionEntry def = entries.get("default");
        if (def != null && def.commandsSet != null) {
            return Set.copyOf(def.commandsSet);
        }

        return Set.of();
    }
}