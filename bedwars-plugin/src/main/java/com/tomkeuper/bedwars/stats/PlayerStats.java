package com.tomkeuper.bedwars.stats;

import com.tomkeuper.bedwars.api.stats.IPlayerStats;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerStats implements IPlayerStats {

    private final UUID uuid;
    private final ModeStats overallStats = new ModeStats();
    private final EnumMap<StatsMode, ModeStats> modeStats = new EnumMap<>(StatsMode.class);

    private String name;

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Instant getFirstPlay() {
        return overallStats.getFirstPlay();
    }

    public void setFirstPlay(Instant firstPlay) {
        overallStats.setFirstPlay(firstPlay);
    }

    public Instant getLastPlay() {
        return overallStats.getLastPlay();
    }

    public void setLastPlay(Instant lastPlay) {
        overallStats.setLastPlay(lastPlay);
    }

    public int getWins() {
        return overallStats.getWins();
    }

    public void setWins(int wins) {
        overallStats.setWins(wins);
    }

    public int getKills() {
        return overallStats.getKills();
    }

    public void setKills(int kills) {
        overallStats.setKills(kills);
    }

    public int getFinalKills() {
        return overallStats.getFinalKills();
    }

    public void setFinalKills(int finalKills) {
        overallStats.setFinalKills(finalKills);
    }

    public int getLosses() {
        return overallStats.getLosses();
    }

    public void setLosses(int losses) {
        overallStats.setLosses(losses);
    }

    public int getDeaths() {
        return overallStats.getDeaths();
    }

    public void setDeaths(int deaths) {
        overallStats.setDeaths(deaths);
    }

    public int getFinalDeaths() {
        return overallStats.getFinalDeaths();
    }

    public void setFinalDeaths(int finalDeaths) {
        overallStats.setFinalDeaths(finalDeaths);
    }

    public int getBedsDestroyed() {
        return overallStats.getBedsDestroyed();
    }

    public void setBedsDestroyed(int bedsDestroyed) {
        overallStats.setBedsDestroyed(bedsDestroyed);
    }

    public int getGamesPlayed() {
        return overallStats.getGamesPlayed();
    }

    public void setGamesPlayed(int gamePlayed) {
        overallStats.setGamesPlayed(gamePlayed);
    }

    public int getTotalKills() {
        return overallStats.getTotalKills();
    }

    public ModeStats getOverallStats() {
        return overallStats;
    }

    public ModeStats getModeStats(StatsMode mode) {
        if (mode == null || mode == StatsMode.OVERALL) {
            return overallStats;
        }
        return modeStats.computeIfAbsent(mode, ignored -> new ModeStats());
    }

    public ModeStats getModeStatsOrEmpty(StatsMode mode) {
        if (mode == null || mode == StatsMode.OVERALL) {
            return overallStats;
        }
        return modeStats.getOrDefault(mode, new ModeStats());
    }

    public Map<StatsMode, ModeStats> getTrackedModeStats() {
        return Collections.unmodifiableMap(modeStats);
    }

    public void setModeStats(StatsMode mode, ModeStats stats) {
        if (mode == null || mode == StatsMode.OVERALL || stats == null) {
            return;
        }
        modeStats.put(mode, stats);
    }

    public void applyToOverallAndMode(StatsMode mode, Consumer<ModeStats> consumer) {
        consumer.accept(overallStats);
        if (mode != null && mode != StatsMode.OVERALL) {
            consumer.accept(getModeStats(mode));
        }
    }
}
