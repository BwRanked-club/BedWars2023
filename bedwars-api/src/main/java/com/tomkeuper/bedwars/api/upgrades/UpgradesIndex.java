package com.tomkeuper.bedwars.api.upgrades;

import com.google.common.collect.ImmutableMap;
import com.tomkeuper.bedwars.api.BedWars;
import org.bukkit.entity.Player;

/**
 * The UpgradesIndex interface represents an upgrade menu in the BedWars game.
 */
public interface UpgradesIndex {

    /**
     * Get the name of the upgrade menu.
     *
     * @return The name of the menu.
     */
    String getName();

    /**
     * Open the upgrade menu for a player.
     * Make sure to use {@link BedWars.TeamUpgradesUtil#setWatchingGUI(Player)}.
     *
     * @param player The player to open the menu for.
     */
    void open(Player player);

    /**
     * Add content to the upgrade menu.
     *
     * @param content The content instance to add.
     * @param slot    The slot where to put the content in the menu.
     * @return `true` if the content was successfully added, `false` if the slot is already in use.
     */
    boolean addContent(MenuContent content, int slot);

    /**
     * Get the total number of tiers in the upgrades.
     *
     * @return The total number of tiers.
     */
    int countTiers();

    /**
     * Get an immutable map of the menu content by slot.
     *
     * @return An immutable map of the menu content by slot.
     */
    ImmutableMap<Integer, MenuContent> getMenuContentBySlot();
}

