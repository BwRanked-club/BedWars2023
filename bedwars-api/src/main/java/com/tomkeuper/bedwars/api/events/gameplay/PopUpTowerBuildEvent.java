package com.tomkeuper.bedwars.api.events.gameplay;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PopUpTowerBuildEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final TeamColor teamColor;
    private final IArena arena;
    private final Block block;

    /**
     * Called when a pop-up tower is building another block
     */
    public PopUpTowerBuildEvent(TeamColor teamColor, IArena arena, Block block) {
        this.teamColor = teamColor;
        this.arena = arena;
        this.block = block;
    }

    /**
     * Get the arena
     */
    public IArena getArena() {
        return arena;
    }

    /**
     * Get the built block
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Get the block's team color
     */
    public TeamColor getTeamColor() {
        return teamColor;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}