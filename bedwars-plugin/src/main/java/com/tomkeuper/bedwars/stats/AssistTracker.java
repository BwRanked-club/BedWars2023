package com.tomkeuper.bedwars.stats;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AssistTracker {

    private static final long ASSIST_WINDOW_MILLIS = 13_000L;
    private static final Map<UUID, Map<UUID, Long>> RECENT_HITS = new ConcurrentHashMap<>();

    private AssistTracker() {
    }

    public static void recordHit(@Nullable Player victim, @Nullable Player attacker, @Nullable IArena arena) {
        if (victim == null || attacker == null || arena == null) return;
        if (victim.getUniqueId().equals(attacker.getUniqueId())) return;
        if (!arena.isPlayer(victim) || !arena.isPlayer(attacker)) return;
        if (arena.isSpectator(victim) || arena.isSpectator(attacker)) return;

        ITeam victimTeam = arena.getTeam(victim);
        ITeam attackerTeam = arena.getTeam(attacker);
        if (victimTeam == null || attackerTeam == null || victimTeam.equals(attackerTeam)) return;

        RECENT_HITS.computeIfAbsent(victim.getUniqueId(), ignored -> new ConcurrentHashMap<>())
                .put(attacker.getUniqueId(), System.currentTimeMillis());
    }

    public static @NotNull List<Player> consumeAssistants(@Nullable Player victim, @Nullable IArena arena, @Nullable Player killer) {
        if (victim == null) return List.of();

        Map<UUID, Long> hits = RECENT_HITS.remove(victim.getUniqueId());
        if (hits == null || hits.isEmpty() || arena == null) {
            return List.of();
        }

        long cutoff = System.currentTimeMillis() - ASSIST_WINDOW_MILLIS;
        ITeam victimTeam = arena.getTeam(victim);
        List<Map.Entry<UUID, Long>> orderedHits = new ArrayList<>(hits.entrySet());
        orderedHits.removeIf(entry -> entry.getValue() == null || entry.getValue() < cutoff);
        orderedHits.sort(Comparator.comparingLong(Map.Entry<UUID, Long>::getValue).reversed());

        List<Player> assistants = new ArrayList<>(orderedHits.size());
        for (Map.Entry<UUID, Long> entry : orderedHits) {
            if (killer != null && killer.getUniqueId().equals(entry.getKey())) continue;

            Player attacker = Bukkit.getPlayer(entry.getKey());
            if (attacker == null || !attacker.isOnline()) continue;
            if (!arena.isPlayer(attacker) || arena.isSpectator(attacker)) continue;

            ITeam attackerTeam = arena.getTeam(attacker);
            if (attackerTeam == null || attackerTeam.equals(victimTeam)) continue;

            assistants.add(attacker);
        }
        return assistants;
    }

    public static void clearVictim(@Nullable UUID victimId) {
        if (victimId == null) return;
        RECENT_HITS.remove(victimId);
    }

    public static void clearPlayer(@Nullable UUID playerId) {
        if (playerId == null) return;
        clearVictim(playerId);
        for (Map<UUID, Long> hits : RECENT_HITS.values()) {
            hits.remove(playerId);
        }
    }
}
