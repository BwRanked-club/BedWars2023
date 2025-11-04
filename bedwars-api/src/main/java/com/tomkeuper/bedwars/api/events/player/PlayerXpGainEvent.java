package com.tomkeuper.bedwars.api.events.player;

import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerXpGainEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    /**
     * -- SETTER --
     *  Set the amount of xp received.
     */
    @Setter
    private int amount;
    private final XpSource xpSource;
    /**
     * -- SETTER --
     *  Cancel event
     */
    @Setter
    private boolean cancelled = false;

    /**
     * Called when a player receives new xp.
     * This only works when the internal Level System is used.
     * Developers can "inject" their own level system.
     *
     * @param player   - target player.
     * @param amount   - amount of xp.
     * @param xpSource - where did the player receive xp from.
     */
    public PlayerXpGainEvent(Player player, int amount, XpSource xpSource) {
        this.player = player;
        this.amount = amount;
        this.xpSource = xpSource;
    }

    /**
     * Get the player that have received new xp.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the amount of xp received.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Get xp source
     */
    public XpSource getXpSource() {
        return xpSource;
    }

    /**
     * Check if event was cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Lets you know why did the player received new xp.
     */
    public enum XpSource {
        PER_MINUTE, PER_TEAMMATE, GAME_WIN, BED_DESTROYED, FINAL_KILL, REGULAR_KILL, OTHER
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
