package com.tomkeuper.bedwars.history;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class HistoryMenuHolder implements InventoryHolder {
    private final UUID targetId;
    private final int page;
    private Inventory inventory;

    public HistoryMenuHolder(UUID targetId, int page) {
        this.targetId = targetId;
        this.page = page;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public int getPage() {
        return page;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

