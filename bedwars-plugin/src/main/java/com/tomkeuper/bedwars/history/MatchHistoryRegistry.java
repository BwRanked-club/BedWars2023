package com.tomkeuper.bedwars.history;

import com.tomkeuper.bedwars.api.arena.IArena;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MatchHistoryRegistry {

    private static final Map<String, UUID> MATCH_BY_ARENA = new ConcurrentHashMap<>();

    private MatchHistoryRegistry() {
    }

    public static UUID get(IArena arena) {
        String key = arenaKey(arena);
        if (key == null) return null;
        return MATCH_BY_ARENA.get(key);
    }

    public static UUID getOrCreate(IArena arena) {
        String key = arenaKey(arena);
        if (key == null) return null;
        return MATCH_BY_ARENA.computeIfAbsent(key, k -> UUID.randomUUID());
    }

    public static UUID clear(IArena arena) {
        String key = arenaKey(arena);
        if (key == null) return null;
        return MATCH_BY_ARENA.remove(key);
    }

    public static UUID clearByKey(String key) {
        if (key == null) return null;
        return MATCH_BY_ARENA.remove(key);
    }

    public static String arenaKey(IArena arena) {
        if (arena == null) return null;
        if (arena.getWorldName() != null) return arena.getWorldName();
        return arena.getArenaName();
    }
}
