package com.tomkeuper.bedwars.api.arena;

import com.tomkeuper.bedwars.api.arena.team.ITeam;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class PlayerReplacementResult {

    private final Status status;
    private final IArena arena;
    private final ITeam team;
    private final Player replacedPlayer;
    private final Player incomingPlayer;

    private PlayerReplacementResult(
            Status status,
            @Nullable IArena arena,
            @Nullable ITeam team,
            @Nullable Player replacedPlayer,
            @Nullable Player incomingPlayer
    ) {
        this.status = status;
        this.arena = arena;
        this.team = team;
        this.replacedPlayer = replacedPlayer;
        this.incomingPlayer = incomingPlayer;
    }

    public static PlayerReplacementResult success(IArena arena, ITeam team, Player replacedPlayer, Player incomingPlayer) {
        return new PlayerReplacementResult(Status.SUCCESS, arena, team, replacedPlayer, incomingPlayer);
    }

    public static PlayerReplacementResult failure(
            Status status,
            @Nullable IArena arena,
            @Nullable ITeam team,
            @Nullable Player replacedPlayer,
            @Nullable Player incomingPlayer
    ) {
        return new PlayerReplacementResult(status, arena, team, replacedPlayer, incomingPlayer);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public Status getStatus() {
        return status;
    }

    public @Nullable IArena getArena() {
        return arena;
    }

    public @Nullable ITeam getTeam() {
        return team;
    }

    public @Nullable Player getReplacedPlayer() {
        return replacedPlayer;
    }

    public @Nullable Player getIncomingPlayer() {
        return incomingPlayer;
    }

    public enum Status {
        SUCCESS,
        ARENA_NOT_FOUND,
        UNSUPPORTED_ARENA_IMPLEMENTATION,
        NOT_PRIMARY_THREAD,
        ARENA_NOT_PLAYING,
        SAME_PLAYER,
        REPLACED_PLAYER_NOT_ACTIVE,
        REPLACED_PLAYER_TEAM_NOT_FOUND,
        NEW_PLAYER_OFFLINE,
        NEW_PLAYER_ALREADY_PLAYING,
        NEW_PLAYER_BUSY,
        TEAM_SLOT_UNAVAILABLE,
        INTERNAL_ERROR
    }
}
