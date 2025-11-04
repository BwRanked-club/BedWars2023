package com.tomkeuper.bedwars.api.upgrades;

import com.tomkeuper.bedwars.api.arena.team.ITeam;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * The MenuContent interface represents a piece of content in a menu.
 * It defines methods for retrieving the display item, handling click events,
 * and getting the name of the menu content.
 */
public interface MenuContent {

    /**
     * Retrieves the display item for this menu content.
     *
     * @param player the player viewing the menu
     * @param team   the team associated with the menu
     * @return the display item
     */
    ItemStack getDisplayItem(Player player, ITeam team);

    /**
     * Handles the click event for a specific upgrade item.
     *
     * @param player           The player who initiated the upgrade.
     * @param clickType        The type of click.
     * @param team             The team associated with the menu.
     * @param forFree          Indicates if the upgrade is obtained for free.
     * @param announcePurchase Indicates whether the purchase should be announced to the team.
     * @param openInv          Indicates whether to open the inventory menu after purchase.
     * @return True if the upgrade was successfully applied, false otherwise.
     */
    boolean onClick(Player player, ClickType clickType, ITeam team, boolean forFree, boolean announcePurchase, boolean announceAlreadyUnlocked, boolean openInv);

    /**
     * Retrieves the name of this menu content.
     *
     * @return the name of the menu content
     */
    String getName();
}

