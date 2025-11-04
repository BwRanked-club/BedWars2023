package com.tomkeuper.bedwars.api.region;

import org.bukkit.Location;

/**
 * The Region interface represents a region in a game world.
 * Implementations of this interface provide methods for checking if a location is within the region
 * and determining if the region is protected.
 */
public interface Region {

    /**
     * Checks if a given location is within the region.
     *
     * @param location The location to check.
     * @return {@code true} if the location is within the region, {@code false} otherwise.
     */
    boolean isInRegion(Location location);

    /**
     * Checks if the region is protected.
     *
     * @return {@code true} if the region is protected, {@code false} otherwise.
     */
    boolean isProtected();
}
