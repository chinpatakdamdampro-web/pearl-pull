package com.ppull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PPullConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ppull.json");

    /** lowercase name -> name as typed */
    private static final Map<String, String> allowed = new ConcurrentHashMap<>();
    private static volatile boolean enabled = true;

    private static final class Data {
        boolean enabled = true;
        List<String> players = new ArrayList<>();
    }

    private PPullConfig() {}

    public static void load() {
        if (!Files.exists(PATH)) return;
        try (Reader r = Files.newBufferedReader(PATH)) {
            Data d = GSON.fromJson(r, Data.class);
            if (d == null) return;
            enabled = d.enabled;
            allowed.clear();
            if (d.players != null) for (String p : d.players) allowed.put(key(p), p);
        } catch (Exception e) {
            System.err.println("[ppull] Failed to read config: " + e);
        }
    }

    public static synchronized void save() {
        Data d = new Data();
        d.enabled = enabled;
        d.players = names();
        try (Writer w = Files.newBufferedWriter(PATH)) {
            GSON.toJson(d, w);
        } catch (IOException e) {
            System.err.println("[ppull] Failed to save config: " + e);
        }
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    public static boolean isAllowed(String name) {
        return name != null && allowed.containsKey(key(name));
    }

    public static boolean add(String name) {
        boolean added = allowed.putIfAbsent(key(name), name) == null;
        if (added) save();
        return added;
    }

    public static boolean remove(String name) {
        boolean removed = allowed.remove(key(name)) != null;
        if (removed) save();
        return removed;
    }

    public static List<String> names() {
        List<String> out = new ArrayList<>(allowed.values());
        out.sort(String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        save();
    }
}
