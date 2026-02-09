package com.tomkeuper.bedwars.history;

import java.util.UUID;

public class MatchHistoryRecord {
    private final long id;
    private final UUID matchId;
    private final UUID playerId;
    private final String playerName;
    private final String arenaName;
    private final String arenaDisplay;
    private final String arenaGroup;
    private final int teamSize;
    private final String mode;
    private final String teamName;
    private final String teamColor;
    private final int placement;
    private final boolean win;
    private final int kills;
    private final int finalKills;
    private final int totalKills;
    private final int deaths;
    private final int finalDeaths;
    private final int bedsDestroyed;
    private final long startedAt;
    private final long endedAt;
    private final int durationSeconds;
    private final String serverId;

    public MatchHistoryRecord(long id,
                              UUID matchId,
                              UUID playerId,
                              String playerName,
                              String arenaName,
                              String arenaDisplay,
                              String arenaGroup,
                              int teamSize,
                              String mode,
                              String teamName,
                              String teamColor,
                              int placement,
                              boolean win,
                              int kills,
                              int finalKills,
                              int totalKills,
                              int deaths,
                              int finalDeaths,
                              int bedsDestroyed,
                              long startedAt,
                              long endedAt,
                              int durationSeconds,
                              String serverId) {
        this.id = id;
        this.matchId = matchId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.arenaName = arenaName;
        this.arenaDisplay = arenaDisplay;
        this.arenaGroup = arenaGroup;
        this.teamSize = teamSize;
        this.mode = mode;
        this.teamName = teamName;
        this.teamColor = teamColor;
        this.placement = placement;
        this.win = win;
        this.kills = kills;
        this.finalKills = finalKills;
        this.totalKills = totalKills;
        this.deaths = deaths;
        this.finalDeaths = finalDeaths;
        this.bedsDestroyed = bedsDestroyed;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
        this.serverId = serverId;
    }

    public long getId() {
        return id;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getArenaName() {
        return arenaName;
    }

    public String getArenaDisplay() {
        return arenaDisplay;
    }

    public String getArenaGroup() {
        return arenaGroup;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public String getMode() {
        return mode;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public int getPlacement() {
        return placement;
    }

    public boolean isWin() {
        return win;
    }

    public int getKills() {
        return kills;
    }

    public int getFinalKills() {
        return finalKills;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getFinalDeaths() {
        return finalDeaths;
    }

    public int getBedsDestroyed() {
        return bedsDestroyed;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getEndedAt() {
        return endedAt;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getServerId() {
        return serverId;
    }
}

