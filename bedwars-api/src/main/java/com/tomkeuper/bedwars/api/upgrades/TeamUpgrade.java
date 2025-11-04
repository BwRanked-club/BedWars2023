package com.tomkeuper.bedwars.api.upgrades;

/**
 * This interface represents a team upgrade in a bed wars mini-game.
 */
public interface TeamUpgrade {

    /**
     * Get the name of the team upgrade.
     *
     * @return The name of the team upgrade.
     */
    String getName();

    /**
     * Get the total number of tiers available for the team upgrade.
     *
     * @return The number of tiers for the team upgrade.
     */
    int getTierCount();
}