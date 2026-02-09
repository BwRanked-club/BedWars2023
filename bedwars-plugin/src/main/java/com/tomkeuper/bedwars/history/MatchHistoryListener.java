package com.tomkeuper.bedwars.history;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaDisableEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaRestartEvent;
import com.tomkeuper.bedwars.api.events.team.TeamEliminatedEvent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.history.MatchHistoryRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MatchHistoryListener implements Listener {

    private final Map<String, List<String>> eliminationOrderByArena = new ConcurrentHashMap<>();

    @EventHandler
    public void onTeamEliminated(TeamEliminatedEvent event) {
        if (!BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_ENABLED)) return;
        IArena arena = event.getArena();
        String key = arenaKey(arena);
        if (key == null) return;

        eliminationOrderByArena.computeIfAbsent(key, k -> new ArrayList<>());
        List<String> order = eliminationOrderByArena.get(key);
        String teamName = event.getTeam().getName();
        if (!order.contains(teamName)) {
            order.add(teamName);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameEnd(GameEndEvent event) {
        if (!BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_MATCH_HISTORY_ENABLED)) return;

        IArena arena = event.getArena();
        String key = arenaKey(arena);
        List<String> eliminated = key == null ? new ArrayList<>() : eliminationOrderByArena.getOrDefault(key, new ArrayList<>());
        UUID matchId = MatchHistoryRegistry.getOrCreate(arena);

        int teamSize = arena.getMaxInTeam();
        String mode = MatchHistoryService.resolveModeLabel(teamSize);
        String serverId = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_SERVER_ID);
        if (serverId == null) serverId = "";
        Instant end = Instant.now();
        Instant start = arena.getStartTime();
        long startedAt = start != null ? start.toEpochMilli() : end.toEpochMilli();
        long endedAt = end.toEpochMilli();
        int durationSeconds = start != null ? (int) Math.max(0, Duration.between(start, end).getSeconds()) : 0;

        Map<String, Integer> placementByTeam = computePlacements(arena, eliminated, event.getTeamWinner());

        Set<UUID> allPlayers = new LinkedHashSet<>();
        allPlayers.addAll(event.getWinners());
        allPlayers.addAll(event.getLosers());

        List<MatchHistoryRecord> records = new ArrayList<>();
        for (UUID playerId : allPlayers) {
            String playerName = resolveName(playerId);
            boolean win = event.getWinners().contains(playerId);

            ITeam team = arena.getExTeam(playerId);
            String teamName = team != null ? team.getDisplayName(Language.getDefaultLanguage()) : "";
            String teamColor = team != null ? team.getColor().chat().toString() : "";
            String teamKey = team != null ? team.getName() : "";

            int placement = placementByTeam.getOrDefault(teamKey, win ? 1 : 2);

            int kills = getKills(arena, playerId, false);
            int finalKills = getKills(arena, playerId, true);
            int totalKills = getTotalKills(arena, playerId, kills, finalKills);
            int deaths = getDeaths(arena, playerId, false);
            int finalDeaths = getDeaths(arena, playerId, true);
            int beds = getBeds(arena, playerId);

            MatchHistoryRecord record = new MatchHistoryRecord(
                    0L,
                    matchId,
                    playerId,
                    playerName,
                    arena.getArenaName(),
                    arena.getDisplayName(),
                    arena.getGroup(),
                    teamSize,
                    mode,
                    teamName,
                    teamColor,
                    placement,
                    win,
                    kills,
                    finalKills,
                    totalKills,
                    deaths,
                    finalDeaths,
                    beds,
                    startedAt,
                    endedAt,
                    durationSeconds,
                    serverId
            );
            records.add(record);
        }

        if (!records.isEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> MatchHistoryService.saveHistory(records));
        }

        if (key != null) {
            eliminationOrderByArena.remove(key);
        }
    }

    @EventHandler
    public void onArenaRestart(ArenaRestartEvent event) {
        String arenaName = event.getArenaName();
        if (arenaName != null) eliminationOrderByArena.remove(arenaName);
        String worldName = event.getWorldName();
        if (worldName != null) eliminationOrderByArena.remove(worldName);
        if (arenaName != null) MatchHistoryRegistry.clearByKey(arenaName);
        if (worldName != null) MatchHistoryRegistry.clearByKey(worldName);
    }

    @EventHandler
    public void onArenaDisable(ArenaDisableEvent event) {
        String worldKey = event.getWorldName();
        if (worldKey != null) eliminationOrderByArena.remove(worldKey);
        String arenaKey = event.getArenaName();
        if (arenaKey != null) eliminationOrderByArena.remove(arenaKey);
        if (arenaKey != null) MatchHistoryRegistry.clearByKey(arenaKey);
        if (worldKey != null) MatchHistoryRegistry.clearByKey(worldKey);
    }

    private static Map<String, Integer> computePlacements(IArena arena, List<String> eliminated, ITeam winnerTeam) {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        int totalTeams = arena.getTeams().size();

        for (int i = 0; i < eliminated.size(); i++) {
            int placement = totalTeams - i;
            result.put(eliminated.get(i), placement);
        }

        if (winnerTeam != null) {
            result.put(winnerTeam.getName(), 1);
        }
        return result;
    }

    private static int getKills(IArena arena, UUID playerId, boolean finalKills) {
        if (arena instanceof Arena bwArena) {
            return bwArena.getPlayerKills(playerId, finalKills);
        }
        return 0;
    }

    private static int getTotalKills(IArena arena, UUID playerId, int kills, int finalKills) {
        if (arena instanceof Arena bwArena) {
            return bwArena.getPlayerTotalKills(playerId);
        }
        return kills + finalKills;
    }

    private static int getDeaths(IArena arena, UUID playerId, boolean finalDeaths) {
        if (arena instanceof Arena bwArena) {
            return bwArena.getPlayerDeaths(playerId, finalDeaths);
        }
        return 0;
    }

    private static int getBeds(IArena arena, UUID playerId) {
        if (arena instanceof Arena bwArena) {
            return bwArena.getPlayerBedsDestroyed(playerId);
        }
        return 0;
    }

    private static String resolveName(UUID playerId) {
        Player online = Bukkit.getPlayer(playerId);
        if (online != null) return online.getName();
        try {
            String name = Bukkit.getOfflinePlayer(playerId).getName();
            return name != null ? name : playerId.toString();
        } catch (Exception ignored) {
        }
        return playerId.toString();
    }

    private static String arenaKey(IArena arena) {
        if (arena == null) return null;
        if (arena.getWorldName() != null) return arena.getWorldName();
        return arena.getArenaName();
    }
}

