package com.tomkeuper.bedwars.history;

import java.util.UUID;

public class MatchHistoryEventRecord {
    private final long id;
    private final UUID matchId;
    private final long eventTime;
    private final String eventType;
    private final UUID actorId;
    private final String actorName;
    private final UUID targetId;
    private final String targetName;
    private final String teamName;
    private final String teamColor;
    private final String meta;

    public MatchHistoryEventRecord(long id,
                                   UUID matchId,
                                   long eventTime,
                                   String eventType,
                                   UUID actorId,
                                   String actorName,
                                   UUID targetId,
                                   String targetName,
                                   String teamName,
                                   String teamColor,
                                   String meta) {
        this.id = id;
        this.matchId = matchId;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.actorId = actorId;
        this.actorName = actorName;
        this.targetId = targetId;
        this.targetName = targetName;
        this.teamName = teamName;
        this.teamColor = teamColor;
        this.meta = meta;
    }

    public long getId() {
        return id;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public long getEventTime() {
        return eventTime;
    }

    public String getEventType() {
        return eventType;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public String getMeta() {
        return meta;
    }
}
