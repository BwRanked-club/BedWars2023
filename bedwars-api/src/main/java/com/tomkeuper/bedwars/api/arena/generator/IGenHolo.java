package com.tomkeuper.bedwars.api.arena.generator;

import org.bukkit.entity.Player;

public interface IGenHolo {

    /**
     * Set timer hologram display text.
     */
    void setTimerName(String timer);

    /**
     * Set tier hologram display text.
     */
    void setTierName(String tier);

    /**
     * Get language iso associated with this hologram.
     */
    String getIso();

    /**
     * Get the player associated with this hologram.
     */
    Player getPlayer();

    /**
     * Get the generator associated with this hologram.
     */
    IGenerator getGenerator();

    /**
     * Update the hologram.
     */
    void update();

    /**
     * This must be called when disabling the generator {@link IGenerator#disable()}
     */
    void destroy();
}
