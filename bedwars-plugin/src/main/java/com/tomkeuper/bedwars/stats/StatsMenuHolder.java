package com.tomkeuper.bedwars.stats;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class StatsMenuHolder implements InventoryHolder {

    private final UUID targetId;
    private Inventory inventory;

    public StatsMenuHolder(UUID targetId) {
        this.targetId = targetId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
