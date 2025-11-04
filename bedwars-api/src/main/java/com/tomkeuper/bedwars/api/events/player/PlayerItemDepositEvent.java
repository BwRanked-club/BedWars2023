package com.tomkeuper.bedwars.api.events.player;

import com.tomkeuper.bedwars.api.arena.IArena;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerItemDepositEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final @NotNull Player player;
    private final @NotNull IArena arena;
    private final @NotNull ItemStack item;
    private final @NotNull Inventory targetInventory;
    private final @NotNull Material containerType;

    @Setter
    @Getter
    private boolean cancelled;

    /**
     * Fired when a player deposits an item stack into a resource chest or ender chest.
     * @param player - player who stored the item.
     * @param arena - player's arena
     * @param item - cloned item stack that was stored.
     * @param targetInventory - inventory where the item was stored.
     * @param containerType - true if the item was stored in an ender chest, false if in a resource chest.
     */
    public PlayerItemDepositEvent(@NotNull Player player,
                                  @NotNull IArena arena,
                                  @NotNull ItemStack item,
                                  @NotNull Inventory targetInventory,
                                  @NotNull Material containerType) {
        this.player = player;
        this.arena = arena;
        this.item = item;
        this.targetInventory = targetInventory;
        this.containerType = containerType;
        this.cancelled = false;
    }

    /**
     * Returns the player who stored the item.
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * Returns the player's arena
     */
    public @NotNull IArena getArena() {
        return arena;
    }

    /**
     * Returns the cloned item stack that was stored.
     */
    public @NotNull ItemStack getItem() {
        return item;
    }

    /**
     * Returns the inventory where the item was stored.
     */
    public @NotNull Inventory getTargetInventory() {
        return targetInventory;
    }

    /**
     * Returns the type of container where the item was stored.
     */
    public @NotNull Material getContainerType() {
        return containerType;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}