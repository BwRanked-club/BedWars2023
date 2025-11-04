package com.tomkeuper.bedwars.api.events.gameplay;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class EggBridgeBuildEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * -- GETTER --
     * Get the block's team color
     */
    private final TeamColor teamColor;
    /**
     * -- GETTER --
     * Get the arena
     */
    private final IArena arena;
    /**
     * -- GETTER --
     * Get the built block
     */
    private final Block block;

    /**
     * Called when the eggBridge is building another block
     * Called when an egg bridge is building another block
     */
    public EggBridgeBuildEvent(TeamColor teamColor, IArena arena, Block block) {
        this.teamColor = teamColor;
        this.arena = arena;
        this.block = block;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
