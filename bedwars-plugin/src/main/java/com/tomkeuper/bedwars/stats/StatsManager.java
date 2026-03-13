package com.tomkeuper.bedwars.stats;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.events.player.PlayerStatChangeEvent;
import com.tomkeuper.bedwars.api.stats.IPlayerStats;
import com.tomkeuper.bedwars.api.stats.IStatsManager;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class StatsManager implements IStatsManager {

    private final Map<UUID, IPlayerStats> stats = new ConcurrentHashMap<>();

    public StatsManager() {
        registerListeners();
    }

    public void remove(UUID uuid) {
        stats.remove(uuid);
    }

    public void put(UUID uuid, IPlayerStats playerStats) {
        stats.put(uuid, playerStats);
    }

    public @Nullable PlayerStats getMutable(UUID uuid) {
        IPlayerStats playerStats = stats.get(uuid);
        if (playerStats instanceof PlayerStats mutable) {
            return mutable;
        }
        return null;
    }

    public void apply(Player player, IArena arena, Consumer<ModeStats> updater) {
        PlayerStats playerStats = getMutable(player.getUniqueId());
        if (playerStats == null) {
            throw new IllegalStateException("Trying to mutate stats data of an unloaded player!");
        }
        playerStats.applyToOverallAndMode(StatsModeResolver.resolve(arena), updater);
    }

    @Override
    public @NotNull IPlayerStats get(UUID uuid) {
        IPlayerStats playerStats = stats.get(uuid);
        if (playerStats == null) {
            throw new IllegalStateException("Trying to get stats data of an unloaded player!");
        }
        return playerStats;
    }

    @Override
    public @Nullable IPlayerStats getUnsafe(UUID uuid) {
        return stats.get(uuid);
    }

    @Override
    public void addPlayerKill(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        PlayerStatChangeEvent ev = new PlayerStatChangeEvent(player, arena, PlayerStatChangeEvent.StatType.KILLS);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        apply(player, arena, stats -> stats.setKills(stats.getKills() + 1));
    }

    @Override
    public void addFinalKill(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        PlayerStatChangeEvent ev = new PlayerStatChangeEvent(player, arena, PlayerStatChangeEvent.StatType.FINAL_KILLS);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        apply(player, arena, stats -> stats.setFinalKills(stats.getFinalKills() + 1));
    }

    @Override
    public void addPlayerDeath(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        PlayerStatChangeEvent ev = new PlayerStatChangeEvent(player, arena, PlayerStatChangeEvent.StatType.DEATHS);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        apply(player, arena, stats -> stats.setDeaths(stats.getDeaths() + 1));
    }

    @Override
    public void addFinalDeath(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        PlayerStatChangeEvent ev = new PlayerStatChangeEvent(player, arena, PlayerStatChangeEvent.StatType.FINAL_DEATHS);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        apply(player, arena, stats -> stats.setFinalDeaths(stats.getFinalDeaths() + 1));
    }

    @Override
    public void addBedBreak(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        PlayerStatChangeEvent ev = new PlayerStatChangeEvent(player, arena, PlayerStatChangeEvent.StatType.BEDS_DESTROYED);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        apply(player, arena, stats -> stats.setBedsDestroyed(stats.getBedsDestroyed() + 1));
    }

    @Override
    public void addWin(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        PlayerStatChangeEvent ev = new PlayerStatChangeEvent(player, arena, PlayerStatChangeEvent.StatType.WINS);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        apply(player, arena, stats -> stats.setWins(stats.getWins() + 1));
    }

    @Override
    public void addLoss(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        PlayerStatChangeEvent ev = new PlayerStatChangeEvent(player, arena, PlayerStatChangeEvent.StatType.LOSSES);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        apply(player, arena, stats -> stats.setLosses(stats.getLosses() + 1));
    }

    @Override
    public void addGamesPlayed(Player player) {
        IArena arena = Arena.getArenaByPlayer(player);
        PlayerStatChangeEvent ev = new PlayerStatChangeEvent(player, arena, PlayerStatChangeEvent.StatType.GAMES_PLAYED);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) {
            return;
        }
        apply(player, arena, stats -> stats.setGamesPlayed(stats.getGamesPlayed() + 1));
    }

    /**
     * Register listeners related to stats cache.
     */
    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new StatsListener(), BedWars.plugin);
    }
}
