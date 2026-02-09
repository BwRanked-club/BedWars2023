package com.tomkeuper.bedwars.history;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class HistoryEventMenuHolder implements InventoryHolder {
    private final UUID targetId;
    private final UUID matchId;
    private final int page;
    private final int historyPage;
    private Inventory inventory;

    public HistoryEventMenuHolder(UUID targetId, UUID matchId, int page, int historyPage) {
        this.targetId = targetId;
        this.matchId = matchId;
        this.page = page;
        this.historyPage = historyPage;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public int getPage() {
        return page;
    }

    public int getHistoryPage() {
        return historyPage;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
