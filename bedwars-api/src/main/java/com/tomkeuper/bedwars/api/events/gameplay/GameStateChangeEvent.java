package com.tomkeuper.bedwars.api.events.gameplay;

import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class GameStateChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private IArena arena;
    private GameState oldState, newState;

    /**
     * Called when the status of the game is changed. Waiting, Starting etc.
     */
    public GameStateChangeEvent(IArena a, GameState oldState, GameState newState) {
        this.arena = a;
        this.oldState = oldState;
        this.newState = newState;
    }


    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
