package com.tomkeuper.bedwars.api.arena.team;

import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
import org.bukkit.entity.Player;

public interface IBedHolo {
    /**
     * Create the hologram for the bed.
     */
    void create();

    /**
     * Hide the hologram for the bed.
     */
    void hide();

    /**
     * Hide the hologram for a specific player.
     */
    void hide(Player player);

    /**
     * Destroy the hologram for the bed.
     */
    void destroy();

    /*+
     * Hide the hologram for a specific player.
     */
    void remove(Player player);

    /**
     * Show the hologram for the bed.
     */
    void show();

    /**
     * Show the hologram for a specific player.
     */
    void show(Player player);

    /**
     * Update the hologram for all players.
     */
    void update();

    /**
     * Update the hologram for a specific player.
     */
    void update(Player player);

    /**
     * Get the main hologram associated with the bed.
     */
    IHologram getHologram();
}